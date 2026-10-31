---
name: Edigen Technical Reference Manual
description: Existing Asciidoctor documentation and plain-text CLI presentation.
colors:
  brick-red: "#ba3925"
  reference-blue: "#2156a5"
  reference-blue-hover: "#1d4b8f"
  caption-red: "#7a2518"
  paper: "#fff"
  ink: "rgba(0,0,0,.8)"
  title-ink: "rgba(0,0,0,.85)"
  code-ink: "rgba(0,0,0,.9)"
  muted-ink: "rgba(0,0,0,.6)"
  contents-surface: "#f8f8f7"
  code-surface: "#f7f7f8"
  table-heading-surface: "#f7f8f7"
  divider: "#e7e7e9"
  rule: "#dddddf"
  table-border: "#dedede"
typography:
  display:
    fontFamily: '"Open Sans","DejaVu Sans",sans-serif'
    fontSize: "2.75em"
    fontWeight: 300
    lineHeight: 1.2
    letterSpacing: "-0.01em"
  headline:
    fontFamily: '"Open Sans","DejaVu Sans",sans-serif'
    fontSize: "2.3125em"
    fontWeight: 300
    lineHeight: 1.2
    letterSpacing: "-0.01em"
  body:
    fontFamily: '"Noto Serif","DejaVu Serif",serif'
    fontSize: "1.0625rem"
    fontWeight: 400
    lineHeight: 1.6
    letterSpacing: "-0.01em"
  code:
    fontFamily: '"Droid Sans Mono","DejaVu Sans Mono",monospace'
    fontSize: "1em"
    fontWeight: 400
    lineHeight: 1.45
  navigation:
    fontFamily: '"Open Sans","DejaVu Sans",sans-serif'
    fontSize: ".95em"
    fontWeight: 400
    lineHeight: 1.3334
rounded:
  document-block: "4px"
spacing:
  page-gutter: ".9375em"
  block-gap: "1.25em"
  code-padding: "1em"
components:
  document-link:
    textColor: "{colors.reference-blue}"
  document-link-hover:
    textColor: "{colors.reference-blue-hover}"
  contents:
    backgroundColor: "{colors.contents-surface}"
    typography: "{typography.navigation}"
    padding: "1.25em 1em"
  code-listing:
    backgroundColor: "{colors.code-surface}"
    textColor: "{colors.code-ink}"
    typography: "{typography.code}"
    rounded: "{rounded.document-block}"
    padding: "{spacing.code-padding}"
  table-heading:
    backgroundColor: "{colors.table-heading-surface}"
    textColor: "{colors.ink}"
    padding: ".5em .625em .625em"
---

# Design System: Edigen

## Overview

**Creative North Star: "Technical Reference Manual"**

Structured, readable, restrained documentation. Serif paragraphs carry explanations;
light sans-serif headings expose the hierarchy; monospace examples preserve the
instruction syntax. Brick Red headings and Reference Blue links distinguish
structure from navigation on a white page.

This records the existing Asciidoctor default theme, not a new application interface.
The CLI uses plain text and inherits its appearance from the terminal. The HTML
styles are extracted from `docs/output/index.html`; `docs/index.adoc` and the `doc`
task in `build.gradle` control the content and rendering. The output is generated,
not tracked source. Source inspection and an up-to-date documentation build support
this record; browser rendering was unavailable during capture.

**Key Characteristics:**

- Clear numbered sections with an anchored table of contents.
- Long-form serif reading with distinct code examples.
- Mostly flat layout and restrained document blocks.
- Responsive navigation supplied by the documentation renderer.

## Colors

Warm headings and conventional blue links sit against quiet neutral surfaces.
Frontmatter values preserve the stylesheet's original color notation.

### Primary

- **Brick Red** (`brick-red`): section headings; the document title uses Title Ink.
- **Caption Red** (`caption-red`): block captions and the contents title.

### Secondary

- **Reference Blue** (`reference-blue`): hyperlinks, shifting to the recorded hover
  color on hover and focus.

### Neutral

- **Paper**: page and table backgrounds.
- **Ink / Title Ink / Code Ink**: body text, document title and lead text, and code.
- **Muted Ink**: document metadata and admonition content.
- **Contents Surface / Code Surface / Table Heading Surface**: navigation, examples,
  and column headers respectively.
- **Divider / Rule / Table Border**: section boundaries, header and admonition rules,
  and table grids.

C4 diagrams retain the colors declared in `docs/_c4-*.adoc`. Those diagram-specific
roles are separate from the document palette.

The companion `.impeccable/design.json` contains generated tonal ramps for palette
previews. Those ramps are illustrative metadata, not additional theme tokens.

## Typography

Open Sans headings contrast with Noto Serif paragraphs and Droid Sans Mono code.
Keep the DejaVu and generic fallbacks: the HTML requests web fonts from Google
Fonts, so the primary fonts are not guaranteed to load in every environment.

The frontmatter records desktop roles. Below the first breakpoint, the document
title is smaller (2.125em) and section headings are smaller (1.6875em). At the first
breakpoint they use the display and headline values above. Subsection headings grow
from 1.375em to 1.6875em.

Paragraphs use the body role, with a paragraph gap (1.25rem). The introductory lead
uses larger text (1.21875em) with the same line-height ratio. Code listings step
from compact text (.8125em), through intermediate text (.90625em), to the code role
at the wide breakpoint. The contents list similarly steps from .9em to .95em.

## Layout

The reading column is centered with a maximum width (62.5em) and the recorded page
gutters. Sections, lists, figures, and listings use the block-gap rhythm. Hierarchy
comes from numbered headings, indentation, whitespace, and thin section dividers.

Below the first breakpoint, the contents appears in document flow. At the first
breakpoint (768px), it becomes a fixed, independently scrollable left sidebar
(15em), with matching body padding. At the wide breakpoint (1280px), the sidebar
and body offset grow (20em). The main column retains its maximum width.

Images scale within their container; code listings permit horizontal scrolling.
Print styles restore document flow, remove the screen reading-width limit and
shadows, and expose external link destinations.

## Elevation & Depth

The current page is mostly flat. Pale fills and thin rules distinguish navigation,
code, tables, and notes. The bundled stylesheet also supports a lightly shadowed
example container; no such container is present in the captured document. Do not
treat that unused renderer style as a general card system.

## Shapes

The page, tables, and navigation are rectangular. Code listings and inline code use
modestly rounded corners, captured by `document-block`. C4 diagrams declare their
own rounded rectangles in the diagram source.

## Components

### Links and contents navigation

Body links are underlined. Contents links omit underlines at rest and underline
when active. Both use the recorded link colors. The stylesheet supplies a thin
dotted keyboard-focus outline. Heading anchors reveal a section symbol on hover.
The contents title uses Caption Red; nested entries use indentation.

### Code listings and inline code

Restrained document blocks use Code Surface, Code Ink, the code font, and the
recorded padding and radius. Inline code uses the same surface with compact
padding (.1em .5ex) and a relative font size (.9375em). Preserve whitespace where
the example requires it. Listings expose their language label on hover.

### Reference tables

Tables have a Paper background, muted heading fill, and thin grid borders (1px).
Headers are bold and left-aligned; body cells use compact padding
(.5625em .625em). Use tables for syntax, argument, and architecture mappings.

### Admonitions

Notes and tips use an uppercase, bold sans-serif label in a separate column (80px).
A thin vertical rule separates it from Muted Ink content with inset padding
(1.125em on the left, 1.25em on the right). These are explanatory document blocks.

### Diagrams and captions

C4 diagrams are generated as SVGs from PlantUML in the AsciiDoc source. Captions
use italic serif text and Caption Red. Keep the diagram's role labels and source
styling together rather than substituting document heading colors.

### CLI help and diagnostics

`src/main/java/net/emustudio/edigen/ui/Help.java` emits `Usage:`, an invocation,
positional-argument descriptions, and `Supported options:`. Option definitions
occupy an aligned field (15 characters). `Edigen.java` emits plain-text diagnostics.
The terminal owns fonts, colors, and width; HTML theme tokens do not apply here.

## Do's and Don'ts

- **Do** preserve the serif, sans-serif, and monospace role distinction.
- **Do** author content and diagrams in AsciiDoc and regenerate the HTML.
- **Do** retain semantic headings, links, tables, and code blocks.
- **Do** keep command-line examples faithful to the implemented help text.
- **Don't** hand-edit generated HTML as the source of a design change.
- **Don't** treat unused styles in the bundled theme as existing product components.
- **Don't** apply the documentation palette to terminal output by assumption.
