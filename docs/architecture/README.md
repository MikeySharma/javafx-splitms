# SplitMS Architecture

This document is the architecture reference for SplitMS. It maps the complete codebase into system, runtime, behavioral, and data views.

## Architecture Scope

- Runtime context and deployment topology.
- Application layering from JavaFX controllers to MySQL.
- Component interactions across navigator, shell, services, and repositories.
- Core behavior flows: auth, navigation, expense creation, and settlement.
- Data model from Flyway migrations V1 through V9.

## Diagram Index

### Structural Views

- [System context](diagrams/system-context.mmd)
- [Runtime layered architecture](diagrams/runtime-layered.mmd)
- [Component map](diagrams/component-map.mmd)
- [Local deployment](diagrams/deployment-local.mmd)

### Behavioral Views

- [Authentication sequence](diagrams/sequence-auth.mmd)
- [Navigation state machine](diagrams/navigation-state.mmd)
- [Navigation content loading sequence](diagrams/sequence-navigation.mmd)
- [Expense creation with splits sequence](diagrams/sequence-expense-create.mmd)
- [Settlement transaction sequence](diagrams/sequence-settlement.mmd)

### Data Views

- [ERD from Flyway schema](diagrams/data-erd.mmd)

## System Context

```mermaid
flowchart LR
	User[User]
	App[SplitMS Desktop App\nJavaFX + Maven]
	DB[(MySQL Database)]
	Env[Environment Variables\nDB_HOST DB_PORT DB_NAME DB_USER DB_PASSWORD]
	Flyway[Flyway Migrations\nV1..V9]
	Seeder[DataSeeder Utility]
	Docker[Docker Compose\nmysql-splitms]

	User -->|uses| App
	App -->|read/write via JDBC| DB
	Env -->|configuration| App
	Flyway -->|applies schema| DB
	Seeder -->|inserts sample data| DB
	Docker -->|runs local database| DB
```

## Runtime Layered Architecture

```mermaid
flowchart TB
	subgraph UI[Presentation Layer]
		V1[FXML Views]
		C1[Controllers]
		VN[ViewNavigator]
		SH[MainShellController]
	end

	subgraph APP[Application Layer]
		SS[SessionManager]
		AS[ApplicationServices]
		US[UserService]
		GS[GroupsService]
		GMS[GroupMembersService]
		ES[ExpensesService]
		CS[CategoriesService]
		TS[TransactionsService]
	end

	subgraph DATA[Data Access Layer]
		RI[Repository Interfaces]
		RJ[Jdbc Repositories]
		DBX[Database.java]
		JPA[Jpa.java]
	end

	subgraph STORE[Persistence]
		MYSQL[(MySQL)]
		MIG[Flyway Migrations]
	end

	V1 --> C1
	C1 --> VN
	C1 --> SH
	C1 --> SS
	C1 --> AS

	AS --> US
	AS --> GS
	AS --> GMS
	AS --> ES
	AS --> CS
	AS --> TS

	US --> RI
	GS --> RI
	GMS --> RI
	ES --> RI
	CS --> RI
	TS --> RI

	RI --> RJ
	RJ --> DBX
	DBX --> MYSQL
	JPA --> MYSQL
	MIG --> MYSQL
```

## Component Map

```mermaid
classDiagram
	class SplitmsApplication
	class ViewNavigator
	class MainShellController
	class SessionManager
	class ApplicationServices

	class UserService
	class GroupsService
	class GroupMembersService
	class ExpensesService
	class CategoriesService
	class TransactionsService

	class UserRepository
	class GroupRepository
	class GroupMembershipRepository
	class ExpenseRepository
	class ExpenseSplitRepository
	class CategoryRepository
	class TransactionRepository

	class Database
	class MySQL

	SplitmsApplication --> ViewNavigator
	ViewNavigator --> MainShellController
	ViewNavigator --> SessionManager
	MainShellController --> SessionManager
	MainShellController --> ApplicationServices

	ApplicationServices --> UserService
	ApplicationServices --> GroupsService
	ApplicationServices --> GroupMembersService
	ApplicationServices --> ExpensesService
	ApplicationServices --> CategoriesService
	ApplicationServices --> TransactionsService

	UserService --> UserRepository
	UserService --> GroupRepository
	UserService --> GroupMembersService
	GroupsService --> GroupRepository
	GroupsService --> GroupMembersService
	GroupMembersService --> GroupMembershipRepository
	GroupMembersService --> UserRepository
	ExpensesService --> ExpenseRepository
	ExpensesService --> ExpenseSplitRepository
	CategoriesService --> CategoryRepository
	TransactionsService --> TransactionRepository

	UserRepository --> Database
	GroupRepository --> Database
	GroupMembershipRepository --> Database
	ExpenseRepository --> Database
	ExpenseSplitRepository --> Database
	CategoryRepository --> Database
	TransactionRepository --> Database
	Database --> MySQL
```

## Local Deployment Topology

```mermaid
flowchart LR
	subgraph DEV[Developer Machine]
		CLI[Maven CLI]
		APP[JVM Process\nSplitMS JavaFX App]
		ENV[Shell and .env]
	end

	subgraph DOCKER[Docker]
		DBCONT[MySQL Container]
	end

	CLI -->|mvn flyway:migrate| DBCONT
	CLI -->|mvn exec:java DataSeeder| DBCONT
	CLI -->|mvn clean javafx:run| APP
	ENV -->|DB config| APP
	APP -->|JDBC| DBCONT
```

## Authentication Sequence

```mermaid
sequenceDiagram
	actor U as User
	participant RC as RegisterController
	participant US as UserService
	participant UR as UserRepository
	participant GR as GroupRepository
	participant GM as GroupMembersService

	U->>RC: Submit name, email, password
	RC->>US: register(name, email, password)
	US->>UR: findByEmail(normalizedEmail)
	UR-->>US: not found
	US->>UR: create(user)
	UR-->>US: userId
	US->>GR: create personal default group
	GR-->>US: groupId
	US->>GM: addMember(groupId, userId)
	US-->>RC: ServiceResult success
	RC-->>U: Show success, navigate to Login

	participant LC as LoginController
	participant SM as SessionManager

	U->>LC: Submit email, password
	LC->>US: login(email, password)
	US->>UR: findAuthByEmail(normalizedEmail)
	UR-->>US: UserAuthRecord
	US-->>LC: ServiceResult success + UserAccount
	LC->>SM: login(userId, name, email)
	LC-->>U: Navigate to Dashboard

	participant MSC as MainShellController

	U->>MSC: Click Logout
	MSC->>SM: logout()
	MSC-->>U: Navigate to Index
```

## Navigation State Machine

```mermaid
stateDiagram-v2
	[*] --> Index
	Index --> Login: Click Login
	Index --> Register: Click Register
	Register --> Login: Register success

	Login --> ShellDashboard: Login success
	Login --> Login: Login failure

	state Shell {
		[*] --> Dashboard
		Dashboard --> Groups: Sidebar click
		Dashboard --> Profile: Sidebar click
		Dashboard --> Expenses: Sidebar click
		Groups --> GroupDetails: Open group
		GroupDetails --> Groups: Back
		Groups --> Dashboard: Sidebar click
		Groups --> Profile: Sidebar click
		Groups --> Expenses: Sidebar click
		Profile --> Dashboard: Sidebar click
		Expenses --> Dashboard: Sidebar click
	}

	ShellDashboard --> Shell
	Shell --> Index: Logout
	Shell --> Login: Session invalid
```

## Navigation Loading Sequence

```mermaid
sequenceDiagram
	actor U as User
	participant VN as ViewNavigator
	participant SM as SessionManager
	participant MS as MainShellController
	participant FX as FXMLLoader

	U->>VN: showDashboard()
	VN->>SM: isLoggedIn()
	alt logged in
		VN->>VN: ensureShellLoaded()
		VN->>FX: load main-shell.fxml
		FX-->>VN: MainShellController
		VN->>MS: setNavigator(this)
		VN->>MS: showDashboardContent()
		MS->>FX: load dashboard-content.fxml
	else not logged in
		VN->>FX: load login.fxml
	end

	U->>MS: open Groups
	MS->>FX: load groups-content.fxml
	U->>MS: open Group Details
	MS->>FX: load group-details-content.fxml
	U->>MS: back to Groups
	MS->>FX: load groups-content.fxml
```

## Expense Creation Sequence

```mermaid
sequenceDiagram
	actor U as User
	participant EC as ExpensesContentController
	participant ES as ExpensesService
	participant ER as JdbcExpenseRepository
	participant SR as JdbcExpenseSplitRepository
	participant DB as MySQL

	U->>EC: Submit expense with member splits
	EC->>ES: createExpenseWithSplits(request)
	ES->>ES: validate amount and split totals
	alt valid splits
		ES->>ER: create expense row
		ER->>DB: INSERT expenses
		DB-->>ER: expenseId
		ES->>SR: create split rows
		SR->>DB: INSERT expense_splits batch
		ES-->>EC: ServiceResult success
		EC-->>U: Refresh list and balances
	else invalid splits
		ES-->>EC: ServiceResult fail
		EC-->>U: Show validation error
	end
```

## Settlement Sequence

```mermaid
sequenceDiagram
	actor U as User
	participant GC as GroupDetailsContentController
	participant TS as TransactionsService
	participant TR as JdbcTransactionRepository
	participant DB as MySQL

	U->>GC: Settle amount from debtor to creditor
	GC->>TS: createTransaction(groupId, fromUser, toUser, amount)
	TS->>TR: create transaction with settled=false
	TR->>DB: INSERT transactions
	DB-->>TR: transactionId
	TS-->>GC: ServiceResult success

	U->>GC: Mark transaction settled
	GC->>TS: settleTransaction(transactionId)
	TS->>TR: settle(transactionId)
	TR->>DB: UPDATE transactions SET settled=true
	TS-->>GC: ServiceResult success
	GC-->>U: Updated balances and settlement status
```

## ERD

```mermaid
erDiagram
	USERS {
		INT id PK
		VARCHAR name
		VARCHAR email UK
		VARCHAR password_hash
		TIMESTAMP created_at
	}

	GROUP_TABLE {
		INT group_id PK
		INT user_id FK
		VARCHAR group_name
		VARCHAR description
		BOOLEAN is_personal_default
		TIMESTAMP created_at
	}

	GROUP_MEMBERS {
		INT id PK
		INT group_id FK
		INT user_id FK
		TIMESTAMP added_at
	}

	CATEGORIES {
		INT category_id PK
		VARCHAR category_name
		VARCHAR category_type
		VARCHAR icon
	}

	EXPENSES {
		INT expense_id PK
		INT group_id FK
		INT payer_id FK
		INT category_id FK
		DECIMAL amount
		DATE expense_date
		VARCHAR description
		VARCHAR title
	}

	EXPENSE_SPLITS {
		INT split_id PK
		INT expense_id FK
		INT user_id FK
		DECIMAL share_amount
		FLOAT share_percentage
	}

	TRANSACTIONS {
		INT transaction_id PK
		INT group_id FK
		INT from_user_id FK
		INT to_user_id FK
		DECIMAL amount
		DATE transaction_date
		BOOLEAN settled
	}

	USERS ||--o{ GROUP_TABLE : owns
	USERS ||--o{ GROUP_MEMBERS : joins
	GROUP_TABLE ||--o{ GROUP_MEMBERS : has
	GROUP_TABLE ||--o{ EXPENSES : includes
	USERS ||--o{ EXPENSES : pays
	CATEGORIES ||--o{ EXPENSES : classifies
	EXPENSES ||--o{ EXPENSE_SPLITS : splits_into
	USERS ||--o{ EXPENSE_SPLITS : owes_share
	GROUP_TABLE ||--o{ TRANSACTIONS : records
	USERS ||--o{ TRANSACTIONS : from_user
	USERS ||--o{ TRANSACTIONS : to_user
```

## Design Invariants

- Service methods return ServiceResult<T> and controllers branch on success.
- SessionManager is the in-memory auth gate used by ViewNavigator.
- ViewNavigator enforces login before protected routes.
- MainShellController swaps content in a persistent shell container.
- Group table is named group in SQL and is quoted in JDBC SQL.
- expense_splits has unique(expense_id, user_id).
- group_members has unique(group_id, user_id).
- Services enforce split sum equals expense amount before persistence.
- User registration creates a personal default group.

## How To Keep Docs Current

Update these diagrams when any of the following changes:

- Navigation flow or protected route behavior.
- Service wiring or repository boundaries.
- Database migration files under src/main/resources/db/migration.
- Local deployment workflow for Docker, Flyway, or app startup.

## Optional Rendering

GitHub renders Mermaid directly in markdown code fences. If you need image exports, you can use Mermaid CLI:

```bash
npx @mermaid-js/mermaid-cli -i docs/architecture/diagrams/system-context.mmd -o docs/architecture/diagrams/system-context.svg
```
