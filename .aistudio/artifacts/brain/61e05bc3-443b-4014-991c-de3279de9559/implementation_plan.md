# Blueprint3D AI: Interactive CAD Drafting, 3D Component Inspector & Full Room Project Persistence

Transform Blueprint3D AI from a preview tool into a full-featured architectural drafting and BIM modeling suite. This milestone introduces a real-time 2D CAD drafting engine with instant 3D extrusion, tap-to-inspect 3D component editing, full blueprint geometry persistence in Room SQLite, and standard 3D model export (OBJ).

## User Review & Critical Decisions

> [!IMPORTANT]
> The recommended next implementation focuses on end-to-end architectural creation: allowing architects and designers to draw custom floor plans from scratch or modify AI-generated blueprints, inspect and tune 3D components in real time, and persist projects in local Room SQLite storage.

- **CAD Editing Experience (Recommended)**: 2D drafting with magnetic grid-snapping (0.25m and 0.5m intervals), orthogonal wall chaining, and intuitive door/window insertion points along wall segments.
- **3D Interaction**: Tap-to-inspect raycasting/hit-testing on 3D elements (walls, doors, windows) with a sliding Inspector Drawer to adjust heights, materials, and openings on the fly.
- **Persistence Strategy**: Extend Room database with a dedicated `blueprints` table storing complete CAD vector geometry alongside project metadata, ensuring offline draft recovery and versioning.

---

## 1. Overview & Core Concept

- **What It Delivers**:
  - **Live CAD Drafting Toolset**: Draw walls with snap-to-grid, insert doors with swing directions, punch windows with sill heights, and label rooms on the 2D blueprint.
  - **Instant Bidirectional 3D Extrusion**: Every wall vertex moved or opening added in 2D immediately regenerates the 3D polygon mesh without requiring page reloads.
  - **3D Object Inspector**: Tap any wall, door, or window in the 3D viewport to inspect structural dimensions, toggle materials (Concrete, Brick, Timber, Glass, Plaster), and adjust door swings or window sizes.
  - **Full Offline Project Library (Room DB)**: Create new projects, duplicate existing designs, rename, and auto-save user drawings in local SQLite storage.
  - **CAD & 3D Model Export**: Export 3D geometry to standard Wavefront OBJ file format and blueprint JSON for external BIM software compatibility.

- **Target Persona**: Architectural designers, civil engineers, interior planners, and homeowners visualizing remodeling plans and structural audits.
- **Key Value**: Closes the loop from AI prompt synthesis to manual precision refinement, interactive 3D inspection, and durable offline local storage.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Drafting a Custom Room**:
   - User switches to **2D Blueprint** tab and taps the floating **Draw Wall** tool.
   - Tapping and dragging on the grid snaps to 0.5m increments with real-time length dimension callouts (e.g. `4.50 m`).
   - Releasing completes the wall segment; chaining continues automatically until double-tapping or closing the loop.
   - User taps **Add Door** or **Add Window**, dragging it onto an existing wall segment to place it.
   - Tapping **3D View** immediately renders the newly constructed geometry with accurate layer visibility and lighting.

2. **3D Component Inspection & Live Editing**:
   - In **3D View**, user taps any wall segment. The tapped wall glows with a cyan holographic wireframe highlight.
   - A bottom **Element Inspector Drawer** slides up, displaying:
     - Element ID and type (`Exterior Load-Bearing Wall`)
     - Dimensions: Length (`6.5 m`), Height (`2.85 m`), Thickness (`0.28 m`)
     - Material Selector: Toggle between Smooth Concrete, Warm Brick, Timber Siding, Glass Curtain, or Drywall.
   - Changes made in the inspector instantly re-extrude the 3D view and update the underlying blueprint.

3. **Managing Projects in Room SQLite Database**:
   - User taps the **Plans** menu in the top app bar to view the **Project Library**.
   - Shows both pre-bundled architectural designs and user-authored designs saved in Room.
   - Options to **Create New Empty Project**, **Duplicate Current**, **Save As**, or **Delete**.
   - Offline status indicator verifies that all floor plans are preserved locally.

4. **Model Export**:
   - User opens the project menu and selects **Export 3D Model (.OBJ)**.
   - The engine generates valid OBJ vertices, normals, and face definitions (`v`, `vn`, `f`) and triggers the standard Android share sheet to save or send the model.

---

## 3. Technical Architecture & Component Hierarchy

```
┌────────────────────────────────────────────────────────────────────────┐
│                        Jetpack Compose UI Layer                        │
│                                                                        │
│   ┌───────────────────────┐  ┌───────────────────────┐  ┌───────────┐  │
│   │   Blueprint2DCanvas   │  │     View3DCanvas      │  │ AiCopilot │  │
│   │  • CAD Tool Palette   │  │  • LayerControlPanel  │  │  Screen   │  │
│   │  • Snapping Grid      │  │  • 3D Hit Testing     │  │  • Prompt │  │
│   │  • Dimension Overlay  │  │  • Element Inspector  │  │  • Audit  │  │
│   └───────────┬───────────┘  └───────────┬───────────┘  └─────┬─────┘  │
│               │                          │                    │        │
└───────────────┼──────────────────────────┼────────────────────┼────────┘
                │                          │                    │
┌───────────────▼──────────────────────────▼────────────────────▼────────┐
│                      ArchitecturalViewModel                            │
│  • Manages active Blueprint state & selected element ID                │
│  • Coordinates 2D CAD edits <-> 3D Engine regeneration                 │
│  • Exposes Room cached models & project library StateFlow              │
└──────────────────────────────────┬─────────────────────────────────────┘
                                   │
┌──────────────────────────────────▼─────────────────────────────────────┐
│                 Domain & Data Persistence Layer                        │
│                                                                        │
│   ┌────────────────────────────────┐  ┌────────────────────────────┐   │
│   │    Architectural3DEngine       │  │    ArchitecturalAiService  │   │
│   │  • Polygon Extrusion           │  │  • Gemini REST Generation  │   │
│   │  • OBJ Model Exporter          │  │  • Structural Audit        │   │
│   │  • Screen-Space Hit Tester     │  └────────────────────────────┘   │
│   └────────────────────────────────┘                                   │
│                                                                        │
│   ┌────────────────────────────────────────────────────────────────┐   │
│   │               Model3dRepository & Room Database                │   │
│   │  • model_3d_metadata (Cached analytics & 3D model summary)     │   │
│   │  • blueprints_storage (Full JSON CAD vector geometry)          │   │
│   └────────────────────────────────────────────────────────────────┘   │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 4. Key Product Decisions & Trade-Offs

- **2D CAD Tool Selection vs 3D Direct Modeling**:
  - *Decision*: Primary geometry authoring (drawing walls, placing openings) is handled in 2D plan view with magnetic grid-snapping, while 3D view focuses on inspection, material customization, and section cutaway tuning.
  - *Why*: 2D plan drafting provides tactile CAD precision on mobile touchscreen interfaces, avoiding the camera occlusion and depth ambiguity common in direct mobile 3D vertex manipulation.

- **Unified Blueprint Storage in Room**:
  - *Decision*: Store full vector floorplan geometry (walls, doors, windows, rooms) serialized as structured JSON inside a `blueprints_storage` Room table, while maintaining `model_3d_metadata` for quick list queries.
  - *Why*: Keeps database migrations lightweight, avoids relational joins across 5 micro-tables for coordinates, and allows instantaneous full-project loading and serialization.

- **Procedural OBJ Exporter**:
  - *Decision*: Implement a zero-dependency in-memory OBJ exporter converting `Poly3D` polygon lists into Wavefront OBJ format.
  - *Why*: Enables architects to import generated models directly into Blender, AutoCAD, SketchUp, or Rhino without cloud dependencies.

---

## 5. Step-by-Step Implementation Strategy

1. **CAD Drafting Tools (`CadDraftingTool.kt`)**:
   - Implement `CadToolMode` enum (`SELECT`, `DRAW_WALL`, `ADD_DOOR`, `ADD_WINDOW`, `ADD_ROOM_TAG`).
   - Add pointer drag gestures with 0.25m / 0.5m grid snapping and live length calculation.
   - Add wall auto-connecting and door/window insertion detection along active wall lines.

2. **3D Element Inspector (`ElementInspectorDrawer.kt`)**:
   - Implement hit-testing on the 3D canvas to detect which polygon or architectural entity was clicked.
   - Build a sliding bottom sheet with controls for Wall Material, Ceiling Height, Opening Dimensions, and Door Swing angle.

3. **Room Database Full Geometry Persistence (`BlueprintStorageEntity.kt`, `BlueprintDao.kt`)**:
   - Add `BlueprintStorageEntity` storing serialized blueprint JSON, title, thumbnail timestamp, and favorite flag.
   - Add DAO operations: `insertOrUpdateBlueprint`, `getAllBlueprints`, `deleteBlueprint`.
   - Update `Model3dRepository` and `ArchitecturalViewModel` to auto-save and load custom projects from Room.

4. **Wavefront OBJ Exporter (`ObjModelExporter.kt`)**:
   - Generate standard `.obj` string with vertex coordinates (`v x y z`), normals (`vn x y z`), and polygonal face indices (`f v1//vn1 v2//vn2 v3//vn3`).
   - Add "Export OBJ" and "Share Model" menu action in the 3D viewport.

5. **Build & Automated Test Verification**:
   - Add Robolectric test cases for Blueprint storage DAO and OBJ export string validation.
   - Run `compile_applet` and test suite to ensure clean compilation and regression-free execution.
