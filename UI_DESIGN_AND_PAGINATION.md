# UI Design and Pagination Guide

This document explains how the current SplitMS JavaFX UI is structured and how page navigation (pagination between screens) is handled.

## 1) UI Design Overview

The current UI follows a **component + page** structure:

- **Reusable UI component**: `Header`
- **Page-level layouts**: `IndexPage`, `LoginPage`
- **Global stylesheet**: `src/main/resources/com/splitms/styles/app.css`

### Main style direction

- Light blue fintech-style background and accents
- Card-based feature section on the index page
- Primary and outline button variants
- Shared typography and spacing through CSS classes

### Core UI files

- `src/main/java/com/splitms/components/Header.java`
- `src/main/java/com/splitms/pages/IndexPage.java`
- `src/main/java/com/splitms/pages/LoginPage.java`
- `src/main/resources/com/splitms/styles/app.css`

---

## 2) Index Page Structure

`IndexPage` is the first screen shown to users.

Layout uses `BorderPane`:

- `top`: reusable `Header`
- `center`: page content (`VBox`) with:
  - Hero title + subtitle
  - Feature cards (`Track`, `Split`, `Learn`)

Feature card icons are loaded from local resources:

- `/com/splitms/assets/icons/track.png`
- `/com/splitms/assets/icons/split.png`
- `/com/splitms/assets/icons/learn.png`

---

## 3) Header Design

`Header` is a reusable `HBox` with:

- Left: app title
- Right: dynamic area (`setRightContent(...)`) for page-specific actions

On `IndexPage`, right actions are:

- `Login` button (`outline-button`)
- `Register` button (`primary-button`)

---

## 4) How Pagination Is Handled (Page Navigation)

In this project, “pagination” is implemented as **page routing/navigation**, not list paging.

### Navigation building blocks

1. `Page` interface
   - Each screen implements:
   - `Scene createScene(PageManager manager)`

2. `PageId` enum
   - Defines route identifiers (currently `INDEX`, `LoGIN`)

3. `PageManager`
   - Registers pages with route IDs
   - Switches scenes via `show(PageId id)`
   - Automatically applies the global stylesheet for each scene

4. `App.start(...)`
   - Registers all pages once
   - Opens initial page with `manager.show(PageId.INDEX)`

### Example flow

- App starts on `INDEX`
- User clicks **Login**
- Button action calls `manager.show(PageId.LoGIN)`
- `PageManager` creates the `LoginPage` scene and sets it on the same stage

---

## 5) Add a New Page (Pattern)

1. Create a class (e.g., `RegisterPage`) implementing `Page`
2. Add route in `PageId` (e.g., `REGISTER`)
3. Register it in `App.start(...)`
4. Navigate using `manager.show(PageId.REGISTER)`

---

## 6) Notes

- DB connectivity check happens in `App.main(...)` before launching UI; it currently warns but does not block startup on failure.
- CSS uses JavaFX properties (`-fx-*`) and is loaded globally by `PageManager`.
