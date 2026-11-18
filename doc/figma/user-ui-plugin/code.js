// ChatApp User UI generator (Figma plugin, run from the Figma desktop app).
// Builds the V1 User UI design described in doc/USER_UI_PLAN.md, in the same visual style as the Admin UI
// (palette and sizes from src/admin-client/.../ui/shared/Ui.java). Everything it creates is named
// "ChatApp User UI · …" so a re-run replaces only its own output.

const PREFIX = "ChatApp User UI · ";
const FONT = "Open Sans"; // Segoe UI (used by the Swing app) is not available in Figma; Open Sans is the closest humanist sans.
const PAL = {
  canvas: "#F5F7FB", rail: "#102345", "rail-active": "#203E72", "rail-muted": "#A6B9D7", ink: "#18263B", muted: "#4D5F78",
  line: "#DCE5F1", soft: "#EDF3FC", blue: "#1B54A8", selected: "#DDEBFF", amber: "#F7EEDC", brown: "#724D17", red: "#A92D35",
  "red-soft": "#FBEAEB", green: "#1F7A4D", "green-soft": "#E3F3EA", white: "#FFFFFF"
};
const TEXT_STYLES = [["Title", 32, "Bold"], ["Heading", 20, "Bold"], ["Subheading", 17, "Bold"], ["Body", 14, "Regular"],
  ["Body Strong", 14, "SemiBold"], ["Label", 13, "SemiBold"], ["Muted", 13, "Regular"], ["Caption", 12, "Regular"],
  ["Caption Strong", 12, "SemiBold"], ["Section", 12, "Bold"], ["Stat", 22, "Bold"]];
const ICON_SVG = {
  brand: '<path d="M7.9 20A9 9 0 1 0 4 16.1L2 22Z"/><path d="M8 12h.01"/><path d="M12 12h.01"/><path d="M16 12h.01"/>',
  chat: '<path d="M7.9 20A9 9 0 1 0 4 16.1L2 22Z"/>',
  group: '<path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M22 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/>',
  friend: '<path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><line x1="19" x2="19" y1="8" y2="14"/><line x1="22" x2="16" y1="11" y2="11"/>',
  account: '<circle cx="12" cy="8" r="5"/><path d="M20 21a8 8 0 0 0-16 0"/>',
  exit: '<path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" x2="9" y1="12" y2="12"/>',
  search: '<circle cx="11" cy="11" r="8"/><path d="m21 21-4.3-4.3"/>',
  lock: '<rect width="18" height="11" x="3" y="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/>',
  chevron: '<path d="m9 18 6-6-6-6"/>',
  down: '<path d="m6 9 6 6 6-6"/>',
  up: '<path d="m18 15-6-6-6 6"/>',
  back: '<path d="m12 19-7-7 7-7"/><path d="M19 12H5"/>',
  send: '<path d="m22 2-7 20-4-9-9-4Z"/><path d="M22 2 11 13"/>',
  sparkles: '<path d="m12 3-1.9 5.8a2 2 0 0 1-1.3 1.3L3 12l5.8 1.9a2 2 0 0 1 1.3 1.3L12 21l1.9-5.8a2 2 0 0 1 1.3-1.3L21 12l-5.8-1.9a2 2 0 0 1-1.3-1.3Z"/>',
  flag: '<path d="M4 15s1-1 4-1 5 2 8 2 4-1 4-1V3s-1 1-4 1-5-2-8-2-4 1-4 1z"/><line x1="4" x2="4" y1="22" y2="15"/>',
  trash: '<path d="M3 6h18"/><path d="M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6"/><path d="M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2"/>',
  plus: '<path d="M5 12h14"/><path d="M12 5v14"/>',
  check: '<path d="M20 6 9 17l-5-5"/>',
  close: '<path d="M18 6 6 18"/><path d="m6 6 12 12"/>',
  block: '<circle cx="12" cy="12" r="10"/><path d="m4.9 4.9 14.2 14.2"/>',
  mail: '<rect width="20" height="16" x="2" y="4" rx="2"/><path d="m22 7-8.97 5.7a1.94 1.94 0 0 1-2.06 0L2 7"/>',
  key: '<circle cx="7.5" cy="15.5" r="5.5"/><path d="m21 2-9.6 9.6"/><path d="m15.5 7.5 3 3L22 7l-3-3"/>',
  edit: '<path d="M17 3a2.85 2.83 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5Z"/>',
  shield: '<path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10"/><path d="m9 12 2 2 4-4"/>',
  history: '<path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"/><path d="M3 3v5h5"/><path d="M12 7v5l4 2"/>',
  wand: '<path d="m21.64 3.64-1.28-1.28a1.21 1.21 0 0 0-1.72 0L2.36 18.64a1.21 1.21 0 0 0 0 1.72l1.28 1.28a1.2 1.2 0 0 0 1.72 0L21.64 5.36a1.2 1.2 0 0 0 0-1.72"/><path d="m14 7 3 3"/>',
  more: '<circle cx="12" cy="12" r="1"/><circle cx="19" cy="12" r="1"/><circle cx="5" cy="12" r="1"/>',
  info: '<circle cx="12" cy="12" r="10"/><path d="M12 16v-4"/><path d="M12 8h.01"/>',
  alert: '<circle cx="12" cy="12" r="10"/><line x1="12" x2="12" y1="8" y2="12"/><line x1="12" x2="12.01" y1="16" y2="16"/>',
  crown: '<path d="m2 4 3 12h14l3-12-6 7-4-7-4 7-6-7zm3 16h14"/>'
};

const V = {}, S = {}, ICON = {}, C = {}, K = {};
const hex = h => ({ r: parseInt(h.slice(1, 3), 16) / 255, g: parseInt(h.slice(3, 5), 16) / 255, b: parseInt(h.slice(5, 7), 16) / 255 });
const P = n => figma.variables.setBoundVariableForPaint({ type: "SOLID", color: hex(PAL[n]) }, "color", V[n]);
const SHADOW = [{ type: "DROP_SHADOW", color: { r: 0.06, g: 0.14, b: 0.27, a: 0.18 }, offset: { x: 0, y: 12 }, radius: 32, spread: 0, visible: true, blendMode: "NORMAL" }];

// ---------------------------------------------------------------- primitives
function stroke(n, color, w = 1) { n.strokes = [P(color)]; n.strokeWeight = w; n.strokeAlign = "INSIDE"; return n; }
function bottomBorder(n, color = "line") { n.strokes = [P(color)]; n.strokeTopWeight = 0; n.strokeLeftWeight = 0; n.strokeRightWeight = 0; n.strokeBottomWeight = 1; n.strokeAlign = "INSIDE"; }
function sideBorder(n, side, color = "line") { n.strokes = [P(color)]; n.strokeTopWeight = 0; n.strokeLeftWeight = 0; n.strokeRightWeight = 0; n.strokeBottomWeight = 0; n[side] = 1; n.strokeAlign = "INSIDE"; }
async function T(str, style = "Body", color = "ink") {
  const t = figma.createText(); await t.setTextStyleIdAsync(S[style].id); t.characters = String(str); t.fills = [P(color)]; return t;
}
function F(name, dir, props) {
  const f = figma.createFrame(); f.name = name; f.fills = []; f.clipsContent = false;
  if (dir) { f.layoutMode = dir; f.primaryAxisSizingMode = "AUTO"; f.counterAxisSizingMode = "AUTO"; }
  if (props) for (const k of Object.keys(props)) f[k] = props[k];
  return f;
}
function pad(f, t, r, b, l) {
  if (r === undefined) r = t; if (b === undefined) b = t; if (l === undefined) l = r;
  f.paddingTop = t; f.paddingRight = r; f.paddingBottom = b; f.paddingLeft = l; return f;
}
function fixW(f, w) { f.resize(w, Math.max(1, f.height)); if (f.layoutMode === "VERTICAL") { f.counterAxisSizingMode = "FIXED"; f.primaryAxisSizingMode = "AUTO"; } else { f.primaryAxisSizingMode = "FIXED"; f.counterAxisSizingMode = "AUTO"; } return f; }
function fixH(f, h) { f.resize(Math.max(1, f.width), h); if (f.layoutMode === "VERTICAL") { f.primaryAxisSizingMode = "FIXED"; f.counterAxisSizingMode = "AUTO"; } else { f.counterAxisSizingMode = "FIXED"; f.primaryAxisSizingMode = "AUTO"; } return f; }
function put(parent, child, h, v) { parent.appendChild(child); if (h) child.layoutSizingHorizontal = h; if (v) child.layoutSizingVertical = v; return child; }
async function para(parent, str, style = "Body", color = "muted") { const t = await T(str, style, color); t.textAutoResize = "HEIGHT"; parent.appendChild(t); t.layoutSizingHorizontal = "FILL"; return t; }
function spacer(parent) { const s = F("Spacer", "HORIZONTAL"); parent.appendChild(s); s.layoutSizingHorizontal = "FILL"; return s; }
function recolor(node, color) { for (const v of node.findAll(n => n.type !== "INSTANCE" && n.type !== "FRAME" && "strokes" in n && n.strokes.length > 0)) v.strokes = [P(color)]; }
function icon(name, size = 18, color = "ink") { const i = ICON[name].createInstance(); i.resize(size, size); recolor(i, color); return i; }
function row(name = "Row", gap = 8) { return F(name, "HORIZONTAL", { itemSpacing: gap, counterAxisAlignItems: "CENTER" }); }
function col(name = "Column", gap = 8) { return F(name, "VERTICAL", { itemSpacing: gap }); }

// ---------------------------------------------------------------- component instances
const BTN_FG = { Primary: "white", Secondary: "ink", Danger: "white", "Danger Outline": "red", Tab: "muted", "Tab Selected": "blue", Disabled: "muted", Rail: "rail-muted" };
function B(label, style = "Secondary", iconName) {
  const i = C.button.defaultVariant.createInstance();
  const props = { Style: style, [K.button.label]: label, [K.button.showIcon]: !!iconName };
  if (iconName) props[K.button.icon] = ICON[iconName].id;
  i.setProperties(props);
  if (iconName) { const ic = i.findOne(n => n.type === "INSTANCE"); if (ic) recolor(ic, BTN_FG[style]); }
  return i;
}
function field(label, value, state = "Default", helper) {
  const i = C.field.defaultVariant.createInstance();
  const props = { State: state, [K.field.label]: label, [K.field.value]: value, [K.field.show]: !!helper };
  if (helper) props[K.field.helper] = helper;
  i.setProperties(props); return i;
}
function pill(label, tone = "Blue") { const i = C.pill.defaultVariant.createInstance(); i.setProperties({ Tone: tone, [K.pill.label]: label }); return i; }
function avatar(initials, online) { const i = C.avatar.createInstance(); i.setProperties({ [K.avatar.initials]: initials, [K.avatar.online]: !!online }); return i; }
function conv(o) {
  const i = C.conv.defaultVariant.createInstance();
  i.setProperties({ Selected: o.sel ? "Yes" : "No", [K.conv.title]: o.title, [K.conv.preview]: o.preview, [K.conv.time]: o.time,
    [K.conv.showUnread]: !!o.unread, [K.conv.unread]: String(o.unread || 1), [K.conv.encrypted]: !!o.enc });
  i.findOne(n => n.type === "INSTANCE" && n.name === "Avatar").setProperties({ [K.avatar.initials]: o.ini, [K.avatar.online]: !!o.online });
  return i;
}
function msg(o) {
  const i = C.msg.defaultVariant.createInstance();
  i.setProperties({ Side: o.mine ? "Mine" : "Theirs", State: o.state || "Default", [K.msg.text]: o.text, [K.msg.meta]: o.meta,
    [K.msg.sender]: o.sender || "Sender", [K.msg.showSender]: !!o.sender });
  return i;
}
function checkbox(on) {
  const f = F("Checkbox", null); f.resize(18, 18); f.cornerRadius = 4;
  if (on) { f.fills = [P("blue")]; const c = icon("check", 12, "white"); f.appendChild(c); c.x = 3; c.y = 3; } else { f.fills = [P("white")]; stroke(f, "muted", 1.5); }
  return f;
}
function toggle(on) {
  const f = F("Toggle", null); f.resize(40, 22); f.cornerRadius = 11; f.fills = [P(on ? "blue" : "line")];
  const k = figma.createEllipse(); k.resize(16, 16); k.fills = [P("white")]; f.appendChild(k); k.x = on ? 21 : 3; k.y = 3; return f;
}
function iconButton(name, style = "Secondary", color) {
  const f = F("Icon button " + name, "HORIZONTAL", { primaryAxisAlignItems: "CENTER", counterAxisAlignItems: "CENTER" });
  f.resize(36, 36); f.primaryAxisSizingMode = "FIXED"; f.counterAxisSizingMode = "FIXED"; f.cornerRadius = 8;
  if (style === "Secondary") { f.fills = [P("white")]; stroke(f, "line"); } else if (style === "Primary") f.fills = [P("blue")];
  f.appendChild(icon(name, 18, color || (style === "Primary" ? "white" : "muted"))); return f;
}
async function searchBox(placeholder, value, width) {
  const f = row("Search", 8); pad(f, 9, 12); f.fills = [P("white")]; stroke(f, value ? "blue" : "line", value ? 2 : 1); f.cornerRadius = 6;
  f.appendChild(icon("search", 16, "muted"));
  const t = await T(value || placeholder, "Body", value ? "ink" : "muted"); f.appendChild(t);
  if (width) fixW(f, width);
  return f;
}
async function banner(parent, tone, iconName, text) {
  const map = { info: ["soft", "blue"], error: ["red-soft", "red"], warn: ["amber", "brown"], ok: ["green-soft", "green"] };
  const [bg, fg] = map[tone];
  const b = row("Banner " + tone, 10); pad(b, 10, 14); b.cornerRadius = 8; b.fills = [P(bg)];
  b.appendChild(icon(iconName, 18, fg));
  parent.appendChild(b); b.layoutSizingHorizontal = "FILL";
  const t = await T(text, "Body", fg); t.textAutoResize = "HEIGHT"; b.appendChild(t); t.layoutSizingHorizontal = "FILL";
  return b;
}
async function card(parent, title, gap = 12) {
  const c = col("Card", gap); pad(c, 16, 18); c.fills = [P("white")]; stroke(c, "line"); c.cornerRadius = 8;
  parent.appendChild(c); c.layoutSizingHorizontal = "FILL";
  if (title) c.appendChild(await T(title, "Subheading"));
  return c;
}
async function info(parent, label, value) {
  const c = col("Info", 2); parent.appendChild(c);
  c.appendChild(await T(label, "Muted", "muted")); c.appendChild(await T(value, "Body")); return c;
}
function tabs(parent, labels, selected) {
  const r = row("Tabs", 4); for (const l of labels) r.appendChild(B(l, l === selected ? "Tab Selected" : "Tab")); parent.appendChild(r); return r;
}
async function header(parent, title, rightNodes = [], backLabel) {
  if (backLabel) parent.appendChild(B(backLabel, "Secondary", "back"));
  const r = row("Header", 8); parent.appendChild(r); r.layoutSizingHorizontal = "FILL";
  r.appendChild(await T(title, "Title")); spacer(r);
  for (const n of rightNodes) r.appendChild(n);
  return r;
}
async function link(text) { return await T(text, "Label", "blue"); }

// Table in the Admin style: soft header, white rows with bottom borders, optional selected rows.
async function table(parent, cols, rows, rowH = 52) {
  const t = col("Table", 0); t.fills = [P("white")]; stroke(t, "line"); t.cornerRadius = 8; t.clipsContent = true;
  parent.appendChild(t); t.layoutSizingHorizontal = "FILL";
  const head = row("Header row", 0); fixH(head, 38); head.fills = [P("soft")]; bottomBorder(head);
  t.appendChild(head); head.layoutSizingHorizontal = "FILL";
  for (const c of cols) await cell(head, c, c.name ? await T(c.name, "Caption Strong", "muted") : null);
  for (const r of rows) {
    const rw = row("Row", 0); fixH(rw, rowH); rw.fills = [P(r.sel ? "selected" : "white")]; bottomBorder(rw);
    t.appendChild(rw); rw.layoutSizingHorizontal = "FILL";
    for (let i = 0; i < cols.length; i++) {
      const v = r.cells[i];
      let node = null;
      if (typeof v === "function") node = await v();
      else if (v !== undefined && v !== null) node = await T(v, i === 0 ? "Body Strong" : "Body", i === 0 ? "ink" : (cols[i].tone || "ink"));
      await cell(rw, cols[i], node);
    }
  }
  return t;
}
async function cell(r, c, node) {
  const f = row("Cell", 8); pad(f, 0, 8, 0, 14); r.appendChild(f);
  if (c.w === "fill") f.layoutSizingHorizontal = "FILL"; else { f.resize(c.w, 10); f.layoutSizingHorizontal = "FIXED"; }
  f.layoutSizingVertical = "FILL";
  if (c.right) f.primaryAxisAlignItems = "MAX";
  if (node) { if (Array.isArray(node)) for (const n of node) f.appendChild(n); else f.appendChild(node); }
}
async function person(name, ini, online, handle) {
  const r = row("Person", 10); r.appendChild(avatar(ini, online));
  const c = col("Name", 0); r.appendChild(c); c.appendChild(await T(name, "Body Strong"));
  if (handle) c.appendChild(await T(handle, "Caption", "muted"));
  return r;
}
function actions(...nodes) { const r = row("Actions", 6); for (const n of nodes) r.appendChild(n); return r; }

// ---------------------------------------------------------------- layout: sections, screens, dialogs
const COLS = 3, W = 1280, H = 800, GAP_X = 120, ROW_PITCH = H + 240, TOP = 340;
async function section(name, title, subtitle, count, y) {
  const rows = Math.ceil(count / COLS);
  const s = figma.createSection(); s.name = PREFIX + name;
  s.resizeWithoutConstraints(80 * 2 + COLS * W + (COLS - 1) * GAP_X, TOP + rows * ROW_PITCH);
  s.x = 0; s.y = y; s.fills = [P("white")];
  page.appendChild(s);
  const t = await T(title, "Title"); s.appendChild(t); t.x = 80; t.y = 64; t.fontSize = 56;
  const st = await T(subtitle, "Body", "muted"); st.fontSize = 20; s.appendChild(st); st.x = 80; st.y = 150;
  return s;
}
async function screen(sec, idx, code, title, desc, nav) {
  const x = 80 + (idx % COLS) * (W + GAP_X), y = TOP + Math.floor(idx / COLS) * ROW_PITCH;
  const cap = await T(code + "  " + title, "Heading"); sec.appendChild(cap); cap.x = x; cap.y = y - 30 - 70;
  const d = await T(desc, "Muted", "muted"); d.textAutoResize = "HEIGHT"; d.resize(W, 20); d.textAutoResize = "HEIGHT"; sec.appendChild(d); d.x = x; d.y = y - 30 - 36;
  const root = F(code + " " + title, "HORIZONTAL"); root.resize(W, H); root.primaryAxisSizingMode = "FIXED"; root.counterAxisSizingMode = "FIXED";
  root.fills = [P("canvas")]; root.clipsContent = true; stroke(root, "line");
  sec.appendChild(root); root.x = x; root.y = y;
  if (nav) { const sb = C.sidebar.defaultVariant.createInstance(); sb.setProperties({ Page: nav }); root.appendChild(sb); sb.layoutSizingVertical = "FILL"; }
  return root;
}
function content(root, gap = 14) { const c = col("Content", gap); pad(c, 30, 32, 20, 32); root.appendChild(c); c.layoutSizingHorizontal = "FILL"; c.layoutSizingVertical = "FILL"; return c; }
async function dialog(root, title, text, build, buttons, width = 460) {
  const scrim = figma.createRectangle(); scrim.name = "Scrim"; scrim.resize(W, H); scrim.fills = [{ type: "SOLID", color: hex(PAL.ink), opacity: 0.45 }];
  root.appendChild(scrim); scrim.layoutPositioning = "ABSOLUTE"; scrim.x = 0; scrim.y = 0;
  const d = col("Dialog", 14); pad(d, 24); d.fills = [P("white")]; d.cornerRadius = 12; stroke(d, "line"); d.effects = SHADOW;
  root.appendChild(d); d.layoutPositioning = "ABSOLUTE"; fixW(d, width);
  d.appendChild(await T(title, "Subheading"));
  if (text) await para(d, text, "Body", "muted");
  if (build) await build(d);
  const a = row("Dialog actions", 8); a.primaryAxisAlignItems = "MAX"; d.appendChild(a); a.layoutSizingHorizontal = "FILL";
  for (const b of buttons) a.appendChild(b);
  d.x = Math.round((W - d.width) / 2); d.y = Math.round((H - d.height) / 2);
  return d;
}
async function authCard(root, width = 420) {
  root.primaryAxisAlignItems = "CENTER"; root.counterAxisAlignItems = "CENTER";
  const c = col("Auth card", 16); pad(c, 30, 34); c.fills = [P("white")]; c.cornerRadius = 12; stroke(c, "line");
  root.appendChild(c); fixW(c, width);
  const brand = row("Brand", 10); c.appendChild(brand);
  const mark = F("Mark", "HORIZONTAL", { primaryAxisAlignItems: "CENTER", counterAxisAlignItems: "CENTER" }); mark.resize(34, 34);
  mark.primaryAxisSizingMode = "FIXED"; mark.counterAxisSizingMode = "FIXED"; mark.cornerRadius = 6; mark.fills = [P("blue")];
  mark.appendChild(icon("brand", 20, "white")); brand.appendChild(mark); brand.appendChild(await T("ChatApp", "Subheading"));
  return c;
}

// ---------------------------------------------------------------- setup: tokens, styles, icons, components
let page;
async function setup() {
  page = figma.root.children.find(p => p.name === "User UI");
  if (!page) { try { page = figma.createPage(); page.name = "User UI"; } catch (e) { page = figma.currentPage; } }
  await figma.setCurrentPageAsync(page);
  for (const n of page.children.filter(n => n.name.startsWith(PREFIX))) n.remove();

  const cols = await figma.variables.getLocalVariableCollectionsAsync();
  let coll = cols.find(c => c.name === "ChatApp tokens");
  if (!coll) coll = figma.variables.createVariableCollection("ChatApp tokens");
  const existing = await figma.variables.getLocalVariablesAsync("COLOR");
  for (const [n, h] of Object.entries(PAL)) {
    let v = existing.find(x => x.name === "color/" + n && x.variableCollectionId === coll.id);
    if (!v) { v = figma.variables.createVariable("color/" + n, coll, "COLOR"); v.scopes = ["ALL_FILLS", "STROKE_COLOR"]; }
    v.setValueForMode(coll.modes[0].modeId, hex(h));
    V[n] = v;
  }
  await Promise.all(["Regular", "SemiBold", "Bold"].map(s => figma.loadFontAsync({ family: FONT, style: s })));
  const styles = await figma.getLocalTextStylesAsync();
  for (const [n, size, st] of TEXT_STYLES) {
    let s = styles.find(x => x.name === n);
    if (!s) { s = figma.createTextStyle(); s.name = n; }
    s.fontName = { family: FONT, style: st }; s.fontSize = size; s.lineHeight = { unit: "PERCENT", value: 135 };
    S[n] = s;
  }
}

async function components(y) {
  const sec = figma.createSection(); sec.name = PREFIX + "Components"; sec.x = 0; sec.y = y; sec.fills = [P("white")]; page.appendChild(sec);
  const title = await T("Components", "Title"); title.fontSize = 56; sec.appendChild(title); title.x = 80; title.y = 64;
  const sub = await T("Local components and tokens for the User UI. Colors are the Admin palette (Ui.java); type is Open Sans standing in for Segoe UI.", "Body", "muted");
  sub.fontSize = 20; sec.appendChild(sub); sub.x = 80; sub.y = 150;
  let Y = 240;
  const place = async (node, label) => { const l = await T(label, "Heading"); sec.appendChild(l); l.x = 80; l.y = Y; sec.appendChild(node); node.x = 80; node.y = Y + 44; Y += 44 + node.height + 70; };

  // Icons
  const iconRow = row("Icons", 16); pad(iconRow, 16); iconRow.fills = [P("white")]; stroke(iconRow, "line"); iconRow.cornerRadius = 8;
  for (const [n, inner] of Object.entries(ICON_SVG)) {
    const svg = `<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#18263B" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" xmlns="http://www.w3.org/2000/svg">${inner}</svg>`;
    const f = figma.createNodeFromSvg(svg);
    for (const ch of f.findAll(() => true)) if ("constraints" in ch) ch.constraints = { horizontal: "SCALE", vertical: "SCALE" };
    const c = figma.createComponentFromNode(f); c.name = "icon/" + n; iconRow.appendChild(c); ICON[n] = c;
  }
  await place(iconRow, "Icons");

  // Button
  const BTN = { Primary: ["blue", "white"], Secondary: ["white", "ink", "line"], Danger: ["red", "white"], "Danger Outline": ["white", "red", "red"],
    Tab: [null, "muted"], "Tab Selected": ["selected", "blue"], Disabled: ["canvas", "muted", "line"], Rail: [null, "rail-muted"] };
  const btns = [];
  for (const [s, [bg, fg, st]] of Object.entries(BTN)) {
    const c = figma.createComponent(); c.name = "Style=" + s; c.layoutMode = "HORIZONTAL"; c.primaryAxisSizingMode = "AUTO"; c.counterAxisSizingMode = "AUTO";
    pad(c, 8, 14); c.cornerRadius = 8; c.itemSpacing = 6; c.counterAxisAlignItems = "CENTER"; c.primaryAxisAlignItems = "CENTER"; c.fills = bg ? [P(bg)] : [];
    if (st) stroke(c, st);
    const ic = icon("plus", 16, fg); ic.name = "Icon"; c.appendChild(ic);
    const t = await T("Button", "Body", fg); t.name = "Label"; c.appendChild(t);
    btns.push(c);
  }
  const btnSet = figma.combineAsVariants(btns, sec); btnSet.name = "Button";
  btnSet.layoutMode = "HORIZONTAL"; btnSet.itemSpacing = 16; pad(btnSet, 16); btnSet.primaryAxisSizingMode = "AUTO"; btnSet.counterAxisSizingMode = "AUTO";
  K.button = { label: btnSet.addComponentProperty("Label", "TEXT", "Button"), showIcon: btnSet.addComponentProperty("Show icon", "BOOLEAN", false),
    icon: btnSet.addComponentProperty("Icon", "INSTANCE_SWAP", ICON.plus.id) };
  for (const c of btnSet.children) {
    c.findOne(n => n.name === "Label").componentPropertyReferences = { characters: K.button.label };
    c.findOne(n => n.name === "Icon").componentPropertyReferences = { visible: K.button.showIcon, mainComponent: K.button.icon };
  }
  C.button = btnSet; await place(btnSet, "Button");

  // Field
  const FS = { Default: ["white", "line", "ink", 1], Placeholder: ["white", "line", "muted", 1], Focused: ["white", "blue", "ink", 2], Error: ["white", "red", "ink", 1], Disabled: ["canvas", "line", "muted", 1] };
  const fields = [];
  for (const [s, [bg, st, fg, w]] of Object.entries(FS)) {
    const c = figma.createComponent(); c.name = "State=" + s; c.layoutMode = "VERTICAL"; c.itemSpacing = 5; c.fills = [];
    c.resize(320, 60); c.counterAxisSizingMode = "FIXED"; c.primaryAxisSizingMode = "AUTO";
    const lab = await T("Label", "Label"); lab.name = "Label"; c.appendChild(lab);
    const box = row("Box", 8); pad(box, 9, 12); box.cornerRadius = 6; box.fills = [P(bg)]; stroke(box, st, w);
    const val = await T("Value", "Body", fg); val.name = "Value"; box.appendChild(val);
    c.appendChild(box); box.layoutSizingHorizontal = "FILL";
    const help = await T("Helper text", "Caption", s === "Error" ? "red" : "muted"); help.name = "Helper"; c.appendChild(help);
    fields.push(c);
  }
  const fieldSet = figma.combineAsVariants(fields, sec); fieldSet.name = "Field";
  fieldSet.layoutMode = "HORIZONTAL"; fieldSet.itemSpacing = 24; pad(fieldSet, 16); fieldSet.primaryAxisSizingMode = "AUTO"; fieldSet.counterAxisSizingMode = "AUTO";
  K.field = { label: fieldSet.addComponentProperty("Label", "TEXT", "Label"), value: fieldSet.addComponentProperty("Value", "TEXT", "Value"),
    helper: fieldSet.addComponentProperty("Helper", "TEXT", "Helper text"), show: fieldSet.addComponentProperty("Show helper", "BOOLEAN", false) };
  for (const c of fieldSet.children) {
    c.findOne(n => n.name === "Label").componentPropertyReferences = { characters: K.field.label };
    c.findOne(n => n.name === "Value").componentPropertyReferences = { characters: K.field.value };
    c.findOne(n => n.name === "Helper").componentPropertyReferences = { characters: K.field.helper, visible: K.field.show };
  }
  C.field = fieldSet; await place(fieldSet, "Field");

  // Avatar
  const av = figma.createComponent(); av.name = "Avatar"; av.resize(36, 36); av.fills = []; av.clipsContent = false;
  const circ = figma.createEllipse(); circ.resize(36, 36); circ.fills = [P("soft")]; stroke(circ, "selected"); av.appendChild(circ);
  const ini = await T("MC", "Caption Strong", "blue"); ini.name = "Initials"; ini.textAutoResize = "NONE"; ini.resize(36, 36);
  ini.textAlignHorizontal = "CENTER"; ini.textAlignVertical = "CENTER"; av.appendChild(ini);
  const dot = figma.createEllipse(); dot.name = "Online dot"; dot.resize(11, 11); dot.fills = [P("green")];
  dot.strokes = [P("white")]; dot.strokeWeight = 2; dot.strokeAlign = "OUTSIDE"; av.appendChild(dot); dot.x = 26; dot.y = 25;
  K.avatar = { initials: av.addComponentProperty("Initials", "TEXT", "MC"), online: av.addComponentProperty("Online", "BOOLEAN", true) };
  ini.componentPropertyReferences = { characters: K.avatar.initials }; dot.componentPropertyReferences = { visible: K.avatar.online };
  C.avatar = av; await place(av, "Avatar");

  // Pill
  const PT = { Blue: ["soft", "blue"], Amber: ["amber", "brown"], Red: ["red-soft", "red"], Green: ["green-soft", "green"], Neutral: ["canvas", "muted"] };
  const pills = [];
  for (const [s, [bg, fg]] of Object.entries(PT)) {
    const c = figma.createComponent(); c.name = "Tone=" + s; c.layoutMode = "HORIZONTAL"; c.primaryAxisSizingMode = "AUTO"; c.counterAxisSizingMode = "AUTO";
    pad(c, 2, 9); c.cornerRadius = 999; c.fills = [P(bg)];
    const t = await T("Label", "Caption Strong", fg); t.name = "Label"; c.appendChild(t); pills.push(c);
  }
  const pillSet = figma.combineAsVariants(pills, sec); pillSet.name = "Pill";
  pillSet.layoutMode = "HORIZONTAL"; pillSet.itemSpacing = 12; pad(pillSet, 16); pillSet.primaryAxisSizingMode = "AUTO"; pillSet.counterAxisSizingMode = "AUTO";
  K.pill = { label: pillSet.addComponentProperty("Label", "TEXT", "Label") };
  for (const c of pillSet.children) c.findOne(n => n.name === "Label").componentPropertyReferences = { characters: K.pill.label };
  C.pill = pillSet; await place(pillSet, "Pill");

  // Conversation item
  const convs = [];
  for (const sel of ["No", "Yes"]) {
    const c = figma.createComponent(); c.name = "Selected=" + sel; c.layoutMode = "HORIZONTAL"; c.itemSpacing = 10; c.counterAxisAlignItems = "CENTER";
    pad(c, 10, 12); c.cornerRadius = 8; c.fills = sel === "Yes" ? [P("selected")] : [];
    c.resize(308, 60); c.primaryAxisSizingMode = "FIXED"; c.counterAxisSizingMode = "AUTO";
    c.appendChild(av.createInstance());
    const text = col("Text", 2); c.appendChild(text); text.layoutSizingHorizontal = "FILL";
    const top = row("Top", 6); text.appendChild(top); top.layoutSizingHorizontal = "FILL";
    const title = await T("Title", "Body Strong"); title.name = "Title"; top.appendChild(title); title.layoutSizingHorizontal = "FILL";
    title.textAutoResize = "HEIGHT"; title.maxLines = 1; title.textTruncation = "ENDING";
    const lk = icon("lock", 13, "muted"); lk.name = "Encrypted"; top.appendChild(lk);
    const time = await T("10:04", "Caption", "muted"); time.name = "Time"; top.appendChild(time);
    const bottom = row("Bottom", 6); text.appendChild(bottom); bottom.layoutSizingHorizontal = "FILL";
    const prev = await T("Preview", "Muted", "muted"); prev.name = "Preview"; bottom.appendChild(prev); prev.layoutSizingHorizontal = "FILL";
    prev.textAutoResize = "HEIGHT"; prev.maxLines = 1; prev.textTruncation = "ENDING";
    const badge = row("Unread", 0); pad(badge, 1, 7); badge.cornerRadius = 999; badge.fills = [P("blue")];
    badge.appendChild(await T("2", "Caption Strong", "white")); bottom.appendChild(badge);
    convs.push(c);
  }
  const convSet = figma.combineAsVariants(convs, sec); convSet.name = "Conversation item";
  convSet.layoutMode = "HORIZONTAL"; convSet.itemSpacing = 24; pad(convSet, 16); convSet.primaryAxisSizingMode = "AUTO"; convSet.counterAxisSizingMode = "AUTO";
  K.conv = { title: convSet.addComponentProperty("Title", "TEXT", "Title"), preview: convSet.addComponentProperty("Preview", "TEXT", "Preview"),
    time: convSet.addComponentProperty("Time", "TEXT", "10:04"), unread: convSet.addComponentProperty("Unread count", "TEXT", "2"),
    showUnread: convSet.addComponentProperty("Show unread", "BOOLEAN", false), encrypted: convSet.addComponentProperty("Encrypted", "BOOLEAN", false) };
  for (const c of convSet.children) {
    c.findOne(n => n.name === "Title").componentPropertyReferences = { characters: K.conv.title };
    c.findOne(n => n.name === "Preview").componentPropertyReferences = { characters: K.conv.preview };
    c.findOne(n => n.name === "Time").componentPropertyReferences = { characters: K.conv.time };
    const u = c.findOne(n => n.name === "Unread"); u.componentPropertyReferences = { visible: K.conv.showUnread };
    u.findOne(n => n.type === "TEXT").componentPropertyReferences = { characters: K.conv.unread };
    c.findOne(n => n.name === "Encrypted").componentPropertyReferences = { visible: K.conv.encrypted };
  }
  C.conv = convSet; await place(convSet, "Conversation item");

  // Message bubble
  const ST = { Default: { Theirs: ["white", "ink", "line", 1], Mine: ["blue", "white", null, 0] },
    Selected: { Theirs: ["selected", "ink", "blue", 2], Mine: ["rail-active", "white", "rail", 2] },
    Match: { Theirs: ["amber", "brown", "brown", 1], Mine: ["amber", "brown", "brown", 1] } };
  const msgs = [];
  for (const side of ["Theirs", "Mine"]) for (const st of ["Default", "Selected", "Match"]) {
    const [bg, fg, sk, w] = ST[st][side];
    const c = figma.createComponent(); c.name = `Side=${side}, State=${st}`; c.layoutMode = "VERTICAL"; c.itemSpacing = 3; c.fills = [];
    c.primaryAxisSizingMode = "AUTO"; c.counterAxisSizingMode = "AUTO"; c.counterAxisAlignItems = side === "Mine" ? "MAX" : "MIN";
    const sender = await T("Sender", "Caption Strong", "muted"); sender.name = "Sender"; c.appendChild(sender);
    const bub = row("Bubble", 8); pad(bub, 8, 12); bub.cornerRadius = 12; bub.fills = [P(bg)]; if (sk) stroke(bub, sk, w);
    const txt = await T("Message text", "Body", fg); txt.name = "Text"; bub.appendChild(txt); c.appendChild(bub);
    const meta = await T("10:04", "Caption", "muted"); meta.name = "Meta"; c.appendChild(meta);
    msgs.push(c);
  }
  const msgSet = figma.combineAsVariants(msgs, sec); msgSet.name = "Message bubble";
  msgSet.layoutMode = "HORIZONTAL"; msgSet.itemSpacing = 24; pad(msgSet, 16); msgSet.primaryAxisSizingMode = "AUTO"; msgSet.counterAxisSizingMode = "AUTO";
  K.msg = { text: msgSet.addComponentProperty("Text", "TEXT", "Message text"), meta: msgSet.addComponentProperty("Meta", "TEXT", "10:04"),
    sender: msgSet.addComponentProperty("Sender", "TEXT", "Sender"), showSender: msgSet.addComponentProperty("Show sender", "BOOLEAN", false) };
  for (const c of msgSet.children) {
    c.findOne(n => n.name === "Text").componentPropertyReferences = { characters: K.msg.text };
    c.findOne(n => n.name === "Meta").componentPropertyReferences = { characters: K.msg.meta };
    c.findOne(n => n.name === "Sender").componentPropertyReferences = { characters: K.msg.sender, visible: K.msg.showSender };
  }
  C.msg = msgSet; await place(msgSet, "Message bubble");

  // Sidebar (same rail as the Admin client: 224 px, brand, sections, user, Sign Out)
  const NAV = [["Messages", [["Chat", "chat"], ["Group", "group"]]], ["People", [["Friend", "friend"]]], ["Settings", [["Account", "account"]]]];
  const bars = [];
  for (const active of ["Chat", "Group", "Friend", "Account"]) {
    const c = figma.createComponent(); c.name = "Page=" + active; c.layoutMode = "VERTICAL"; c.itemSpacing = 0; c.fills = [P("rail")];
    pad(c, 16, 16, 15, 16); c.resize(224, H); c.primaryAxisSizingMode = "FIXED"; c.counterAxisSizingMode = "FIXED";
    const brand = row("Brand", 10); c.appendChild(brand);
    const mark = F("Mark", "HORIZONTAL", { primaryAxisAlignItems: "CENTER", counterAxisAlignItems: "CENTER" }); mark.resize(34, 34);
    mark.primaryAxisSizingMode = "FIXED"; mark.counterAxisSizingMode = "FIXED"; mark.cornerRadius = 6; mark.fills = [P("blue")];
    mark.appendChild(icon("brand", 20, "white")); brand.appendChild(mark); brand.appendChild(await T("ChatApp", "Subheading", "white"));
    const gapTop = F("Gap", "VERTICAL"); gapTop.resize(1, 15); gapTop.primaryAxisSizingMode = "FIXED"; c.appendChild(gapTop);
    for (const [secName, items] of NAV) {
      const sl = await T(secName, "Section", "rail-muted"); const slw = row("Section " + secName, 0); pad(slw, 12, 8, 4, 8); slw.appendChild(sl); c.appendChild(slw);
      for (const [name, ic] of items) {
        const on = name === active;
        const it = row("Nav " + name, 10); pad(it, 0, 10); fixH(it, 39); it.cornerRadius = 8; it.fills = on ? [P("rail-active")] : [];
        c.appendChild(it); it.layoutSizingHorizontal = "FILL";
        it.appendChild(icon(ic, 18, on ? "white" : "rail-muted")); it.appendChild(await T(name, "Body", on ? "white" : "rail-muted"));
      }
    }
    const grow = F("Spacer", "VERTICAL"); c.appendChild(grow); grow.layoutSizingHorizontal = "FILL"; grow.layoutSizingVertical = "FILL";
    const div = figma.createRectangle(); div.name = "Divider"; div.resize(192, 1); div.fills = [P("rail-active")]; c.appendChild(div);
    const me = row("Signed-in user", 10); pad(me, 12, 4, 6, 4); c.appendChild(me);
    me.appendChild(avatar("MC", true));
    const who = col("Who", 0); me.appendChild(who); who.appendChild(await T("Maya Chen", "Body Strong", "white")); who.appendChild(await T("@mayachen", "Caption", "rail-muted"));
    const out = row("Sign Out", 10); pad(out, 8, 10); c.appendChild(out);
    out.appendChild(icon("exit", 17, "rail-muted")); out.appendChild(await T("Sign Out", "Body", "rail-muted"));
    bars.push(c);
  }
  const barSet = figma.combineAsVariants(bars, sec); barSet.name = "Sidebar";
  barSet.layoutMode = "HORIZONTAL"; barSet.itemSpacing = 24; pad(barSet, 16); barSet.primaryAxisSizingMode = "AUTO"; barSet.counterAxisSizingMode = "AUTO";
  C.sidebar = barSet; await place(barSet, "Sidebar");

  for (const s of [btnSet, fieldSet, pillSet, convSet, msgSet, barSet]) { s.fills = [P("white")]; stroke(s, "line"); s.cornerRadius = 8; }
  sec.resizeWithoutConstraints(Math.max(3200, barSet.width + 160), Y + 40);
  return sec;
}

// ---------------------------------------------------------------- sample data (matches the Admin preview data)
const CONVS = [
  { title: "Noah Patel", ini: "NP", online: true, preview: "I'll review the database section today.", time: "10:05" },
  { title: "Project Team", ini: "PT", preview: "Noah: I added my comments to the draft.", time: "09:30", unread: 2 },
  { title: "Ethan Park", ini: "EP", preview: "Buy now! Reply for the promo code.", time: "10:16", unread: 1 },
  { title: "Java Study Group", ini: "JS", preview: "Sofia: Quiz review at 7pm?", time: "Yesterday", enc: true },
  { title: "Ava Brooks", ini: "AB", online: true, preview: "The screenshot list is ready.", time: "Yesterday" },
  { title: "Linh Nguyen", ini: "LN", preview: "You: I'll send the slides tonight.", time: "Nov 8" }
];

// Chat page layout: conversation column + conversation pane.
async function chatLayout(root, o) {
  const left = col("Conversations", 10); pad(left, 26, 16, 16, 16); left.fills = [P("white")]; sideBorder(left, "strokeRightWeight");
  root.appendChild(left); fixW(left, 344); left.layoutSizingVertical = "FILL";
  left.appendChild(await T("Chat", "Title"));
  const sb = await searchBox("Search all messages", o.globalQuery); left.appendChild(sb); sb.layoutSizingHorizontal = "FILL";
  if (o.results) { await o.results(left); }
  else {
    const fl = await searchBox("Filter conversations by name", o.filter); left.appendChild(fl); fl.layoutSizingHorizontal = "FILL";
    const list = col("List", 2); left.appendChild(list); list.layoutSizingHorizontal = "FILL";
    for (const c of CONVS) { const i = conv({ ...c, sel: c.title === o.active }); list.appendChild(i); i.layoutSizingHorizontal = "FILL"; }
  }
  const pane = col("Conversation", 0); root.appendChild(pane); pane.layoutSizingHorizontal = "FILL"; pane.layoutSizingVertical = "FILL";
  if (o.empty) return { left, pane };
  const head = row("Conversation header", 12); pad(head, 14, 24); head.fills = [P("white")]; bottomBorder(head);
  pane.appendChild(head); head.layoutSizingHorizontal = "FILL";
  head.appendChild(avatar(o.ini, o.online));
  const hc = col("Title", 1); head.appendChild(hc);
  const tr = row("Title row", 8); hc.appendChild(tr); tr.appendChild(await T(o.title, "Subheading"));
  if (o.enc) tr.appendChild(pill("Encrypted", "Green"));
  hc.appendChild(await T(o.status, "Caption", o.online ? "green" : "muted"));
  spacer(head);
  for (const n of (o.headerActions || [B("Search in chat", "Secondary", "search"), B("Clear history", "Secondary", "history")])) head.appendChild(n);
  if (o.searchBar) await o.searchBar(pane);
  const body = col("Messages", 12); pad(body, 20, 24); pane.appendChild(body); body.layoutSizingHorizontal = "FILL"; body.layoutSizingVertical = "FILL";
  body.clipsContent = true;
  const day = row("Day", 0); day.primaryAxisAlignItems = "CENTER"; body.appendChild(day); day.layoutSizingHorizontal = "FILL";
  day.appendChild(pill(o.day || "Today", "Neutral"));
  for (const m of o.messages) {
    const r = row("Message row", 10); r.primaryAxisAlignItems = m.mine ? "MAX" : "MIN"; r.counterAxisAlignItems = "MAX";
    body.appendChild(r); r.layoutSizingHorizontal = "FILL";
    if (o.selecting) r.appendChild(checkbox(m.state === "Selected"));
    if (!m.mine) r.appendChild(avatar(m.ini || o.ini, false));
    r.appendChild(msg(m));
  }
  if (o.panel) await o.panel(pane);
  if (o.bottom) await o.bottom(pane); else await composer(pane, o.draft);
  return { left, pane };
}
async function composer(pane, draft) {
  const c = row("Composer", 8); pad(c, 12, 24); c.fills = [P("white")]; sideBorder(c, "strokeTopWeight");
  pane.appendChild(c); c.layoutSizingHorizontal = "FILL";
  const input = row("Input", 8); pad(input, 10, 12); input.cornerRadius = 8; input.fills = [P("white")]; stroke(input, draft ? "blue" : "line", draft ? 2 : 1);
  c.appendChild(input); input.layoutSizingHorizontal = "FILL";
  input.appendChild(await T(draft || "Type a message…", "Body", draft ? "ink" : "muted"));
  c.appendChild(B("Suggest", "Secondary", "sparkles")); c.appendChild(B("Improve", "Secondary", "wand")); c.appendChild(B("Send", "Primary", "send"));
}

// ---------------------------------------------------------------- AUTH (F01, F02, F04)
async function auth(y) {
  const sec = await section("Auth", "Pre-login", "Separate from the Admin Login. F01 Register · F02 Log in · F04 Reset password by email.", 7, y);
  let r, c;
  r = await screen(sec, 0, "A1", "Log in", "F02 · Default state. Links to Register and Forgot password.", null);
  c = await authCard(r);
  c.appendChild(await T("Sign in", "Title")); await para(c, "Chat with your friends and groups.", "Muted", "muted");
  for (const f of [field("Username", "Enter your username", "Placeholder"), field("Password", "Enter your password", "Placeholder")]) put(c, f, "FILL");
  let lr = row("Forgot", 0); lr.primaryAxisAlignItems = "MAX"; put(c, lr, "FILL"); lr.appendChild(await link("Forgot password?"));
  put(c, B("Sign in", "Primary"), "FILL");
  lr = row("Register link", 6); lr.primaryAxisAlignItems = "CENTER"; put(c, lr, "FILL"); lr.appendChild(await T("New to ChatApp?", "Muted", "muted")); lr.appendChild(await link("Create account"));

  r = await screen(sec, 1, "A2", "Log in · missing fields", "F02 · Both fields are required; inline errors under each field.", null);
  c = await authCard(r);
  c.appendChild(await T("Sign in", "Title"));
  put(c, field("Username", "Enter your username", "Error", "Enter your username."), "FILL");
  put(c, field("Password", "Enter your password", "Error", "Enter your password."), "FILL");
  put(c, B("Sign in", "Primary"), "FILL");

  r = await screen(sec, 2, "A3", "Log in · wrong password / locked account", "F02 + F13 · Server errors shown as a banner. A locked account cannot sign in until an admin unlocks it.", null);
  c = await authCard(r);
  c.appendChild(await T("Sign in", "Title"));
  await banner(c, "error", "lock", "This account is locked by an administrator. Contact support to restore access.");
  put(c, field("Username", "linhng"), "FILL"); put(c, field("Password", "••••••••"), "FILL");
  put(c, B("Sign in", "Primary"), "FILL");
  await para(c, "Wrong password variant: “Incorrect username or password.”", "Caption", "muted");

  r = await screen(sec, 3, "A4", "Register", "F01 · Profile fields from UserAccount; address and birth date are optional.", null);
  c = await authCard(r, 720);
  c.appendChild(await T("Create account", "Title"));
  let grid = F("Fields", "HORIZONTAL", { layoutWrap: "WRAP", itemSpacing: 16, counterAxisSpacing: 12 }); put(c, grid, "FILL");
  for (const f of [field("Username", "mayachen"), field("Full name", "Maya Chen"), field("Email", "maya.chen@example.com"), field("Gender", "Female  ▾"),
    field("Date of birth (optional)", "YYYY-MM-DD", "Placeholder"), field("Address (optional)", "Street, city", "Placeholder"),
    field("Password", "••••••••", "Default", "At least 8 characters."), field("Confirm password", "••••••••")]) { grid.appendChild(f); f.resize(318, f.height); }
  put(c, B("Create account", "Primary"), "FILL");
  lr = row("Sign in link", 6); lr.primaryAxisAlignItems = "CENTER"; put(c, lr, "FILL"); lr.appendChild(await T("Already have an account?", "Muted", "muted")); lr.appendChild(await link("Sign in"));

  r = await screen(sec, 4, "A5", "Register · validation errors", "F01 · Unique username and email, valid email, matching passwords (same rules as Admin “Add user”).", null);
  c = await authCard(r, 720);
  c.appendChild(await T("Create account", "Title"));
  grid = F("Fields", "HORIZONTAL", { layoutWrap: "WRAP", itemSpacing: 16, counterAxisSpacing: 12 }); put(c, grid, "FILL");
  for (const f of [field("Username", "noahp", "Error", "This username is already taken."), field("Full name", "Maya Chen"),
    field("Email", "maya.chen@", "Error", "Enter a valid email address."), field("Gender", "Female  ▾"),
    field("Password", "••••••••"), field("Confirm password", "•••••", "Error", "Passwords do not match.")]) { grid.appendChild(f); f.resize(318, f.height); }
  put(c, B("Create account", "Primary"), "FILL");

  r = await screen(sec, 5, "A6", "Forgot password", "F04 · The server generates a random password and emails it.", null);
  c = await authCard(r);
  c.appendChild(B("Back to sign in", "Secondary", "back"));
  c.appendChild(await T("Reset password", "Title"));
  await para(c, "Enter the email on your account. We'll generate a new random password and send it to you.", "Body", "muted");
  put(c, field("Email", "maya.chen@example.com", "Focused"), "FILL");
  put(c, B("Send new password", "Primary", "mail"), "FILL");

  r = await screen(sec, 6, "A7", "Forgot password · email sent", "F04 · Confirmation; in V1 preview the generated email is shown in the app.", null);
  c = await authCard(r, 520);
  c.appendChild(await T("Check your email", "Title"));
  await banner(c, "ok", "check", "A new password was sent to maya.chen@example.com.");
  const mail = col("Email preview", 6); pad(mail, 14, 16); mail.fills = [P("soft")]; mail.cornerRadius = 8; put(c, mail, "FILL");
  mail.appendChild(await T("From: ChatApp <no-reply@chatapp.local>", "Caption", "muted"));
  mail.appendChild(await T("To: maya.chen@example.com", "Caption", "muted"));
  mail.appendChild(await T("Subject: Your new ChatApp password", "Body Strong"));
  await para(mail, "Hi Maya, your password was reset. Your new password is K7m#pQ2v. Sign in and change it in Account › Change password.", "Body", "ink");
  put(c, B("Back to sign in", "Primary"), "FILL");
  return sec;
}

// ---------------------------------------------------------------- CHAT (F10, F11, F12f)
const NOAH = [
  { text: "Hi Maya, did you get the draft report?", meta: "09:58", ini: "NP" },
  { mine: true, text: "Yes! I'm reading it now.", meta: "10:01" },
  { text: "I'll review the database section today.", meta: "10:05", ini: "NP" },
  { mine: true, text: "Great, I'll check the screenshots then.", meta: "10:06 · Seen live" }
];
async function chat(y) {
  const sec = await section("Chat", "Chat", "Private and group conversations. F11a–h chat features · F10 report spam · F12f group chat.", 11, y);
  let r;
  r = await screen(sec, 0, "C1", "Private chat · friend online", "F11a, F11c · Real-time: the friend is online, so messages arrive live (“Seen live”).", "Chat");
  await chatLayout(r, { active: "Noah Patel", title: "Noah Patel", ini: "NP", online: true, status: "● Online · messages are delivered instantly", messages: NOAH });

  r = await screen(sec, 1, "C2", "Private chat · friend offline", "F11b · Offline friend: messages are stored and delivered when they come online.", "Chat");
  await chatLayout(r, { active: "Linh Nguyen", title: "Linh Nguyen", ini: "LN", online: false, status: "Offline · last seen Nov 8, 21:16", day: "Nov 8",
    messages: [{ text: "Can you share the class slides?", meta: "20:40", ini: "LN" }, { mine: true, text: "Sure, I'll send the slides tonight.", meta: "21:02 · Delivered when online" }],
    panel: async pane => { const w = row("Offline note", 0); pad(w, 0, 24, 12, 24); pane.appendChild(w); w.layoutSizingHorizontal = "FILL"; await banner(w, "info", "info", "Linh is offline. Your messages will be waiting when Linh signs in."); } });

  r = await screen(sec, 2, "C3", "Group chat · encrypted group", "F12f, F12g · Group messages show sender names; an encrypted group shows a badge.", "Chat");
  await chatLayout(r, { active: "Java Study Group", title: "Java Study Group", ini: "JS", enc: true, status: "4 members · 2 online", day: "Yesterday",
    messages: [{ text: "Quiz review at 7pm?", meta: "18:10", sender: "Sofia Reyes", ini: "SR" }, { text: "Works for me.", meta: "18:12", sender: "Noah Patel", ini: "NP" },
      { mine: true, text: "I'll bring the practice questions.", meta: "18:15" }, { text: "Thanks Maya!", meta: "18:16", sender: "Ethan Park", ini: "EP" }] });

  r = await screen(sec, 3, "C4", "Select messages · delete or report", "F11e, F10 · Selection mode: delete selected messages for me; Report spam needs exactly one received message.", "Chat");
  await chatLayout(r, { active: "Ethan Park", title: "Ethan Park", ini: "EP", status: "Offline", selecting: true,
    messages: [{ text: "Hey, are you free this weekend?", meta: "10:12", ini: "EP" }, { text: "Buy now! Reply for the promo code.", meta: "10:16", ini: "EP", state: "Selected" },
      { mine: true, text: "Who is this?", meta: "10:18 · Delivered when online" }],
    bottom: async pane => {
      const bar = row("Selection bar", 8); pad(bar, 12, 24); bar.fills = [P("soft")]; sideBorder(bar, "strokeTopWeight"); pane.appendChild(bar); bar.layoutSizingHorizontal = "FILL";
      bar.appendChild(await T("1 message selected", "Body Strong")); spacer(bar);
      bar.appendChild(B("Delete for me", "Danger Outline", "trash")); bar.appendChild(B("Report spam", "Danger", "flag")); bar.appendChild(B("Cancel", "Secondary"));
    } });

  r = await screen(sec, 4, "C5", "Report spam dialog", "F10 · Reports the sender and the selected message to administrators (SpamReport).", "Chat");
  await chatLayout(r, { active: "Ethan Park", title: "Ethan Park", ini: "EP", status: "Offline", messages: [{ text: "Buy now! Reply for the promo code.", meta: "10:16", ini: "EP", state: "Selected" }] });
  await dialog(r, "Report spam", "Report Ethan Park (@ethanp) for this message? Administrators will review it and may lock the account.", async d => {
    const q = col("Quoted", 4); pad(q, 12, 14); q.fills = [P("soft")]; q.cornerRadius = 8; put(d, q, "FILL");
    q.appendChild(await T("Ethan Park · 10:16", "Caption Strong", "muted")); q.appendChild(await T("Buy now! Reply for the promo code.", "Body"));
    d.appendChild(await T("Reason", "Label"));
    for (const [txt, on] of [["Unwanted promotions or ads", true], ["Repeated unsolicited messages", false], ["Other", false]]) {
      const o = row("Reason", 10); o.appendChild(checkbox(on)); o.appendChild(await T(txt, "Body")); d.appendChild(o);
    }
  }, [B("Cancel", "Secondary"), B("Report", "Danger", "flag")]);

  r = await screen(sec, 5, "C6", "Clear history confirm", "F11d · Clears the whole history for me only (historyClearedAt); the other person keeps theirs.", "Chat");
  await chatLayout(r, { active: "Noah Patel", title: "Noah Patel", ini: "NP", online: true, status: "● Online", messages: NOAH });
  await dialog(r, "Clear chat history?", "All messages in this conversation will be removed for you. Noah Patel will still see them. This can't be undone.", null,
    [B("Cancel", "Secondary"), B("Clear history", "Danger", "trash")]);

  r = await screen(sec, 6, "C7", "Search in this chat", "F11f · Find text in one conversation and jump between matches (2 of 3).", "Chat");
  await chatLayout(r, { active: "Noah Patel", title: "Noah Patel", ini: "NP", online: true, status: "● Online",
    headerActions: [B("Clear history", "Secondary", "history")],
    searchBar: async pane => {
      const s = row("Search bar", 8); pad(s, 10, 24); s.fills = [P("soft")]; bottomBorder(s); pane.appendChild(s); s.layoutSizingHorizontal = "FILL";
      const box = await searchBox("Search in this chat", "report"); s.appendChild(box); box.layoutSizingHorizontal = "FILL";
      s.appendChild(await T("2 of 3", "Body Strong", "muted")); s.appendChild(iconButton("up")); s.appendChild(iconButton("down")); s.appendChild(iconButton("close"));
    },
    messages: [{ text: "Hi Maya, did you get the draft report?", meta: "09:58", ini: "NP" }, { mine: true, text: "Yes! I'm reading the report now.", meta: "10:01", state: "Match" },
      { text: "I'll review the database section today.", meta: "10:05", ini: "NP" }] });

  r = await screen(sec, 7, "C8", "Search all conversations", "F11g · Results across every chat; opening a result jumps to and highlights the message.", "Chat");
  await chatLayout(r, { active: "Project Team", title: "Project Team", ini: "PT", status: "6 members · 3 online", globalQuery: "draft",
    results: async left => {
      left.appendChild(await T("4 results in 3 conversations", "Caption Strong", "muted"));
      const hits = [["Project Team", "Maya: I've uploaded the draft report.", "Nov 8", true], ["Project Team", "Noah: I added my comments to the draft.", "09:30", false],
        ["Noah Patel", "Hi Maya, did you get the draft report?", "09:58", false], ["Ava Brooks", "Draft screenshots are in the folder.", "Nov 7", false]];
      for (const [t, s, time, sel] of hits) {
        const h = col("Result", 3); pad(h, 10, 12); h.cornerRadius = 8; h.fills = sel ? [P("selected")] : []; put(left, h, "FILL");
        const tr = row("Top", 6); put(h, tr, "FILL"); tr.appendChild(await T(t, "Body Strong")); spacer(tr); tr.appendChild(await T(time, "Caption", "muted"));
        const sn = await T(s, "Muted", "muted"); const i = s.toLowerCase().indexOf("draft");
        sn.setRangeFontName(i, i + 5, { family: FONT, style: "Bold" }); sn.setRangeFills(i, i + 5, [P("blue")]); h.appendChild(sn);
      }
    },
    messages: [{ mine: true, text: "I've uploaded the draft report.", meta: "Nov 8 · 15:20", state: "Match" },
      { text: "I'll review the database section today.", meta: "15:28", sender: "Noah Patel", ini: "NP" },
      { text: "The screenshot list is ready.", meta: "Nov 9 · 08:45", sender: "Ava Brooks", ini: "AB" }], day: "Nov 8" });

  r = await screen(sec, 8, "C9", "AI suggest replies", "F11h · The LLM suggests replies from the recent messages; the user picks one to insert.", "Chat");
  await chatLayout(r, { active: "Noah Patel", title: "Noah Patel", ini: "NP", online: true, status: "● Online", messages: NOAH.slice(0, 3),
    panel: async pane => {
      const w = col("Suggestions", 10); pad(w, 0, 24, 12, 24); pane.appendChild(w); w.layoutSizingHorizontal = "FILL";
      const p = await card(w, null, 10); stroke(p, "blue");
      const h = row("Head", 8); put(p, h, "FILL"); h.appendChild(icon("sparkles", 18, "blue")); h.appendChild(await T("Suggested replies", "Body Strong")); spacer(h); h.appendChild(iconButton("close", "None"));
      for (const s of ["Thanks Noah! Let me know if anything needs fixing.", "Great — I'll update the ERD section after your review.", "Perfect, can we sync at 3pm?"]) {
        const sr = row("Suggestion", 10); pad(sr, 8, 12); sr.cornerRadius = 8; sr.fills = [P("soft")]; put(p, sr, "FILL");
        const t = await T(s, "Body"); sr.appendChild(t); t.layoutSizingHorizontal = "FILL"; sr.appendChild(B("Insert", "Secondary"));
      }
      p.appendChild(await T("Generated by the AI assistant. Review before sending.", "Caption", "muted"));
    } });

  r = await screen(sec, 9, "C10", "AI improve draft", "F11h · The LLM rewrites the draft; the user replaces the draft or keeps the original.", "Chat");
  await chatLayout(r, { active: "Noah Patel", title: "Noah Patel", ini: "NP", online: true, status: "● Online", messages: NOAH.slice(0, 3), draft: "can u send me the report tmr pls",
    panel: async pane => {
      const w = col("Improve", 10); pad(w, 0, 24, 12, 24); pane.appendChild(w); w.layoutSizingHorizontal = "FILL";
      const p = await card(w, null, 10); stroke(p, "blue");
      const h = row("Head", 8); put(p, h, "FILL"); h.appendChild(icon("wand", 18, "blue")); h.appendChild(await T("Improve message", "Body Strong")); spacer(h);
      for (const t of ["Friendly", "Formal", "Shorter"]) h.appendChild(B(t, t === "Friendly" ? "Tab Selected" : "Tab"));
      const cmp = row("Compare", 12); cmp.counterAxisAlignItems = "MIN"; put(p, cmp, "FILL");
      for (const [lab, txt, bg] of [["Original", "can u send me the report tmr pls", "canvas"], ["Improved", "Could you send me the report tomorrow, please? Thanks!", "soft"]]) {
        const b = col(lab, 4); pad(b, 10, 12); b.cornerRadius = 8; b.fills = [P(bg)]; put(cmp, b, "FILL");
        b.appendChild(await T(lab, "Caption Strong", "muted")); await para(b, txt, "Body", "ink");
      }
      const a = row("Actions", 8); a.primaryAxisAlignItems = "MAX"; put(p, a, "FILL");
      a.appendChild(B("Keep original", "Secondary")); a.appendChild(B("Try again", "Secondary", "sparkles")); a.appendChild(B("Replace draft", "Primary", "check"));
    } });

  r = await screen(sec, 10, "C11", "No conversation selected", "Empty state when the Chat page opens without a selected conversation.", "Chat");
  const { pane } = await chatLayout(r, { empty: true });
  pane.primaryAxisAlignItems = "CENTER"; pane.counterAxisAlignItems = "CENTER"; pane.itemSpacing = 10;
  pane.appendChild(icon("chat", 48, "muted")); pane.appendChild(await T("Select a conversation", "Subheading"));
  pane.appendChild(await T("Or start one from Friend › Chat.", "Muted", "muted"));
  return sec;
}

// ---------------------------------------------------------------- GROUP (F12a–e, g)
const MEMBERS = [["Maya Chen", "MC", "@mayachen", "Group admin", true, "Nov 8, 2026"], ["Noah Patel", "NP", "@noahp", "Group admin", true, "Nov 7, 2026"],
  ["Ava Brooks", "AB", "@avab", "Member", true, "Nov 7, 2026"], ["Ethan Park", "EP", "@ethanp", "Member", false, "Nov 7, 2026"],
  ["Linh Nguyen", "LN", "@linhng", "Member", false, "Nov 7, 2026"], ["Sofia Reyes", "SR", "@sofiareyes", "Member", false, "Nov 7, 2026"]];
async function groupDetail(r, admin) {
  const c = content(r, 12);
  const name = admin ? "Project Team" : "Java Study Group";
  await header(c, name, [B("Open chat", "Primary", "chat"), B("Rename", "Secondary", "edit")], "Groups");
  c.appendChild(await T(admin ? "Created Nov 7, 2026 · Created by Noah Patel (@noahp) · 6 members · You are a group admin"
    : "Created Oct 30, 2026 · Created by Sofia Reyes (@sofiareyes) · 4 members · You are a member", "Muted", "muted"));
  if (!admin) await banner(c, "info", "info", "Only group admins can make admins, remove members or change encryption.");
  const enc = await card(c, null, 6); const er = row("Encryption", 12); put(enc, er, "FILL");
  er.appendChild(icon("lock", 20, admin ? "muted" : "green"));
  const et = col("Text", 2); er.appendChild(et); et.layoutSizingHorizontal = "FILL";
  et.appendChild(await T("Encrypted group", "Body Strong"));
  et.appendChild(await T(admin ? "Off · messages are stored readable on the server." : "On · messages are encrypted; only members can read them.", "Muted", "muted"));
  er.appendChild(toggle(!admin)); if (!admin) er.children[er.children.length - 1].opacity = 0.5;
  const mh = row("Members header", 8); put(c, mh, "FILL"); mh.appendChild(await T("Members", "Subheading")); spacer(mh);
  mh.appendChild(B("Add member", admin ? "Primary" : "Secondary", "plus"));
  const list = admin ? MEMBERS : MEMBERS.filter(m => ["Maya Chen", "Noah Patel", "Ethan Park", "Sofia Reyes"].includes(m[0])).map(m => m[0] === "Sofia Reyes" ? [...m.slice(0, 3), "Group admin", m[4], m[5]] : [...m.slice(0, 3), "Member", m[4], m[5]]);
  await table(c, [{ name: "Member", w: "fill" }, { name: "Group role", w: 140 }, { name: "Status", w: 110 }, { name: "Joined", w: 130 }, { name: "", w: 230, right: true }],
    list.map(m => ({ cells: [() => person(m[0], m[1], m[4], m[2]), () => pill(m[3], m[3] === "Group admin" ? "Blue" : "Neutral"),
      () => pill(m[4] ? "Online" : "Offline", m[4] ? "Green" : "Neutral"), m[5],
      () => m[0] === "Maya Chen" ? T("You", "Muted", "muted") : actions(B(m[3] === "Group admin" ? "Admin" : "Make admin", admin && m[3] !== "Group admin" ? "Secondary" : "Disabled", "crown"),
        B("Remove", admin ? "Danger Outline" : "Disabled", "close"))] })), 56);
}
async function groups(y) {
  const sec = await section("Group", "Group", "Group management only (messaging is on Chat). F12a create · b rename · c add · d make admin · e remove (admins only) · g encryption.", 9, y);
  let r, c;
  r = await screen(sec, 0, "G1", "My groups", "List with name filter and sorting; Create group starts F12a.", "Group");
  c = content(r);
  await header(c, "Group", [B("Create group", "Primary", "plus")]);
  const f = row("Filters", 8); c.appendChild(f); f.appendChild(await searchBox("Search group name", null, 320));
  const G = [["Project Team", "6", "Group admin", false, "Nov 7, 2026"], ["Campus Events", "3", "Member", false, "Nov 1, 2026"], ["Java Study Group", "4", "Member", true, "Oct 30, 2026"], ["Class Announcements", "4", "Member", false, "Oct 24, 2026"]];
  await table(c, [{ name: "Group name", w: "fill" }, { name: "Members", w: 110 }, { name: "My role", w: 150 }, { name: "Encryption", w: 150 }, { name: "Created", w: 150 }, { name: "", w: 50 }],
    G.map(g => ({ cells: [g[0], g[1], () => pill(g[2], g[2] === "Group admin" ? "Blue" : "Neutral"), () => g[3] ? pill("Encrypted", "Green") : T("Off", "Body", "muted"), g[4], () => icon("chevron", 16, "muted")] })), 55);
  c.appendChild(await T("4 groups", "Muted", "muted"));

  r = await screen(sec, 1, "G2", "Create group", "F12a · Name plus members picked by username; at least one other member is required.", "Group");
  c = content(r);
  await header(c, "Create group", [], "Groups");
  let cd = await card(c, "New group", 14);
  put(cd, field("Group name", "Final presentation"), "FILL");
  cd.appendChild(await T("Members", "Label"));
  let sb = await searchBox("Add by username or name", "av"); put(cd, sb, "FILL");
  const chips = row("Chips", 8); cd.appendChild(chips);
  for (const n of ["Noah Patel  ×", "Ava Brooks  ×"]) chips.appendChild(pill(n, "Blue"));
  await table(cd, [{ name: "Suggestions", w: "fill" }, { name: "", w: 160, right: true }],
    [{ cells: [() => person("Ava Brooks", "AB", true, "@avab"), () => T("Added", "Muted", "muted")] }, { cells: [() => person("Dave Lin", "DL", false, "@davelin"), () => B("Add", "Secondary", "plus")] }], 52);
  let a = row("Actions", 8); a.primaryAxisAlignItems = "MAX"; put(cd, a, "FILL"); a.appendChild(B("Cancel", "Secondary")); a.appendChild(B("Create group", "Primary"));

  r = await screen(sec, 2, "G3", "Create group · validation", "F12a · Error when no other member is added (rule enforced by the server).", "Group");
  c = content(r);
  await header(c, "Create group", [], "Groups");
  cd = await card(c, "New group", 14);
  put(cd, field("Group name", "Final presentation"), "FILL");
  put(cd, field("Members", "Add by username or name", "Error", "Add at least one other member."), "FILL");
  a = row("Actions", 8); a.primaryAxisAlignItems = "MAX"; put(cd, a, "FILL"); a.appendChild(B("Cancel", "Secondary")); a.appendChild(B("Create group", "Primary"));

  r = await screen(sec, 3, "G4", "Group detail · admin view", "F12b–e, g · Admins can rename, add, make admin, remove and turn on encryption.", "Group");
  await groupDetail(r, true);
  r = await screen(sec, 4, "G5", "Group detail · member view", "F12e · Non-admins see the same page with admin actions disabled.", "Group");
  await groupDetail(r, false);

  r = await screen(sec, 5, "G6", "Add member dialog", "F12c · Search people by username; existing members are marked.", "Group");
  await groupDetail(r, true);
  await dialog(r, "Add member", "Add people to Project Team.", async d => {
    put(d, await searchBox("Search username or name", "is"), "FILL");
    await table(d, [{ name: "Person", w: "fill" }, { name: "", w: 130, right: true }],
      [{ cells: [() => person("Isabel Cruz", "IC", false, "@isabelc"), () => B("Add", "Primary", "plus")] },
        { cells: [() => person("Linh Nguyen", "LN", false, "@linhng"), () => T("Already a member", "Caption", "muted")] }], 52);
  }, [B("Done", "Secondary")], 520);

  r = await screen(sec, 6, "G7", "Rename group dialog", "F12b · Rename the group; members see the new name.", "Group");
  await groupDetail(r, true);
  await dialog(r, "Rename group", null, async d => { put(d, field("Group name", "Project Team — Final", "Focused"), "FILL"); }, [B("Cancel", "Secondary"), B("Save", "Primary")]);

  r = await screen(sec, 7, "G8", "Remove member confirm", "F12e · Only group admins can remove members.", "Group");
  await groupDetail(r, true);
  await dialog(r, "Remove Ethan Park?", "Ethan Park (@ethanp) will leave Project Team and can no longer read new messages.", null, [B("Cancel", "Secondary"), B("Remove", "Danger")]);

  r = await screen(sec, 8, "G9", "Turn on encryption", "F12g · Admin enables encryption for the group (see Messenger encrypted chats).", "Group");
  await groupDetail(r, true);
  await dialog(r, "Turn on encryption?", "New messages in Project Team will be encrypted so only members can read them. Older messages stay as they are, and the AI assistant can't read encrypted messages.",
    null, [B("Cancel", "Secondary"), B("Turn on", "Primary", "lock")]);
  return sec;
}

// ---------------------------------------------------------------- FRIEND (F06–F09)
const FRIENDS = [["Noah Patel", "NP", "@noahp", true, "Nov 8, 2026"], ["Ava Brooks", "AB", "@avab", true, "Nov 8, 2026"],
  ["Linh Nguyen", "LN", "@linhng", false, "Nov 9, 2026"], ["Ethan Park", "EP", "@ethanp", false, "Nov 8, 2026"]];
async function friendTab(r, tab) {
  const c = content(r);
  await header(c, "Friend", []);
  tabs(c, ["Friends", "Online", "Requests", "Find people"], tab);
  return c;
}
async function friendsTable(c, list, rowSel) {
  await table(c, [{ name: "Full name", w: "fill" }, { name: "Status", w: 120 }, { name: "Friends since", w: 150 }, { name: "", w: 330, right: true }],
    list.map(f => ({ sel: rowSel === f[0], cells: [() => person(f[0], f[1], f[3], f[2]), () => pill(f[3] ? "Online" : "Offline", f[3] ? "Green" : "Neutral"), f[4],
      () => actions(B("Chat", "Secondary", "chat"), B("Unfriend", "Secondary"), B("Block", "Danger Outline", "block"))] })), 56);
}
async function friends(y) {
  const sec = await section("Friend", "Friend", "F06 friends (filter, status, unfriend, block) · F07 requests · F08 online friends · F09 find people.", 9, y);
  let r, c, f;
  r = await screen(sec, 0, "F1", "Friends", "F06a, c, d · Filter by name, online/offline status, Chat, Unfriend, Block.", "Friend");
  c = await friendTab(r, "Friends");
  f = row("Filters", 8); c.appendChild(f); f.appendChild(await searchBox("Filter friends by name", null, 320));
  await friendsTable(c, FRIENDS); c.appendChild(await T("4 friends · 2 online", "Muted", "muted"));

  r = await screen(sec, 1, "F2", "Friends · no match", "F06a · Empty state when the name filter matches nobody.", "Friend");
  c = await friendTab(r, "Friends");
  f = row("Filters", 8); c.appendChild(f); f.appendChild(await searchBox("Filter friends by name", "zoe", 320));
  const e = col("Empty", 8); pad(e, 60, 0); e.counterAxisAlignItems = "CENTER"; put(c, e, "FILL");
  e.appendChild(icon("search", 40, "muted")); e.appendChild(await T("No friends match “zoe”", "Subheading"));
  e.appendChild(await T("Try another name, or look for new people in Find people.", "Muted", "muted")); e.appendChild(B("Find people", "Primary", "friend"));

  r = await screen(sec, 2, "F3", "Unfriend confirm", "F06c · Removes the friendship; either person can send a new request later.", "Friend");
  c = await friendTab(r, "Friends"); await friendsTable(c, FRIENDS, "Linh Nguyen");
  await dialog(r, "Unfriend Linh Nguyen?", "You will no longer be friends with Linh Nguyen (@linhng). Your chat history stays.", null, [B("Cancel", "Secondary"), B("Unfriend", "Danger")]);

  r = await screen(sec, 3, "F4", "Block confirm", "F06d · Block also unfriends; the blocked person can't send requests or find you.", "Friend");
  c = await friendTab(r, "Friends"); await friendsTable(c, FRIENDS, "Ethan Park");
  await dialog(r, "Block Ethan Park?", "Ethan Park (@ethanp) will be removed from your friends, can't send you friend requests, and won't see you in search.", null,
    [B("Cancel", "Secondary"), B("Block", "Danger", "block")]);

  r = await screen(sec, 4, "F5", "Online friends · create group", "F08 · Online friends only; select several to create a group (opens Group › Create).", "Friend");
  c = await friendTab(r, "Online");
  f = row("Filters", 8); put(c, f, "FILL"); f.appendChild(await searchBox("Filter by name", null, 320)); spacer(f);
  f.appendChild(await T("2 selected", "Body Strong")); f.appendChild(B("Create group", "Primary", "group"));
  const on = [["Noah Patel", "NP", "@noahp", true], ["Ava Brooks", "AB", "@avab", true], ["Isabel Cruz", "IC", "@isabelc", true]];
  await table(c, [{ name: "", w: 50 }, { name: "Full name", w: "fill" }, { name: "Status", w: 140 }, { name: "", w: 140, right: true }],
    on.map((p, i) => ({ sel: i < 2, cells: [() => checkbox(i < 2), () => person(p[0], p[1], true, p[2]), () => pill("Online", "Green"), () => B("Chat", "Secondary", "chat")] })), 56);

  r = await screen(sec, 5, "F6", "Friend requests", "F07 · Received requests with Accept / Decline and a name filter; sent requests are pending.", "Friend");
  c = await friendTab(r, "Requests");
  f = row("Filters", 8); c.appendChild(f); f.appendChild(await searchBox("Filter requests by name", null, 320));
  c.appendChild(await T("Received · 2", "Subheading"));
  await table(c, [{ name: "From", w: "fill" }, { name: "Sent", w: 170 }, { name: "", w: 230, right: true }],
    [{ cells: [() => person("Sofia Reyes", "SR", false, "@sofiareyes"), "Nov 10, 2026 · 18:20", () => actions(B("Accept", "Primary", "check"), B("Decline", "Secondary"))] },
      { cells: [() => person("Owen Tran", "OT", false, "@owentran"), "Nov 9, 2026 · 09:02", () => actions(B("Accept", "Primary", "check"), B("Decline", "Secondary"))] }], 56);
  c.appendChild(await T("Sent · 1", "Subheading"));
  await table(c, [{ name: "To", w: "fill" }, { name: "Sent", w: 170 }, { name: "", w: 230, right: true }],
    [{ cells: [() => person("Isabel Cruz", "IC", true, "@isabelc"), "Nov 10, 2026 · 08:15", () => pill("Pending", "Amber")] }], 56);

  r = await screen(sec, 6, "F7", "Find people", "F09, F06b · Search by username or name; the action depends on the relationship.", "Friend");
  c = await friendTab(r, "Find people");
  f = row("Search", 8); put(c, f, "FILL"); const sbox = await searchBox("Search username or name", "an"); f.appendChild(sbox); fixW(sbox, 420);
  await banner(c, "info", "info", "People who blocked you don't appear in search.");
  const people = [["Ana Lopez", "AL", "@analopez", "Not connected", () => actions(B("Add friend", "Primary", "friend"), B("Chat", "Secondary", "chat"), B("Block", "Danger Outline", "block"))],
    ["Ethan Park", "EP", "@ethanp", "Friend", () => actions(B("Chat", "Secondary", "chat"), B("Create group", "Secondary", "group"))],
    ["Owen Tran", "OT", "@owentran", "Request received", () => actions(B("Accept", "Primary", "check"), B("Chat", "Secondary", "chat"))],
    ["Isabel Cruz", "IC", "@isabelc", "Request sent", () => actions(pill("Pending", "Amber"), B("Chat", "Secondary", "chat"))],
    ["Daniel Kang", "DK", "@dkang", "Blocked by you", () => T("Blocked", "Muted", "muted")]];
  const tone = { "Not connected": "Neutral", Friend: "Blue", "Request received": "Amber", "Request sent": "Amber", "Blocked by you": "Red" };
  await table(c, [{ name: "Person", w: "fill" }, { name: "Relationship", w: 170 }, { name: "", w: 380, right: true }],
    people.map(p => ({ cells: [() => person(p[0], p[1], false, p[2]), () => pill(p[3], tone[p[3]]), p[4]] })), 56);

  r = await screen(sec, 7, "F8", "Find people · request sent", "F06b · After Add friend the row switches to Pending and a confirmation appears.", "Friend");
  c = await friendTab(r, "Find people");
  f = row("Search", 8); c.appendChild(f); const sb2 = await searchBox("Search username or name", "ana"); f.appendChild(sb2); fixW(sb2, 420);
  await banner(c, "ok", "check", "Friend request sent to Ana Lopez.");
  await table(c, [{ name: "Person", w: "fill" }, { name: "Relationship", w: 170 }, { name: "", w: 380, right: true }],
    [{ cells: [() => person("Ana Lopez", "AL", false, "@analopez"), () => pill("Request sent", "Amber"), () => actions(pill("Pending", "Amber"), B("Chat", "Secondary", "chat"))] }], 56);

  r = await screen(sec, 8, "F9", "Find people · no results", "F09 · No match (also what a blocked user sees when searching for you).", "Friend");
  c = await friendTab(r, "Find people");
  f = row("Search", 8); c.appendChild(f); const sb3 = await searchBox("Search username or name", "zzq"); f.appendChild(sb3); fixW(sb3, 420);
  const e2 = col("Empty", 8); pad(e2, 60, 0); e2.counterAxisAlignItems = "CENTER"; put(c, e2, "FILL");
  e2.appendChild(icon("search", 40, "muted")); e2.appendChild(await T("No people match “zzq”", "Subheading"));
  e2.appendChild(await T("Check the username or try a full name.", "Muted", "muted"));
  return sec;
}

// ---------------------------------------------------------------- ACCOUNT (F03, F05)
async function profileHead(c) {
  const h = row("Profile head", 16); c.appendChild(h);
  const big = F("Avatar large", "HORIZONTAL", { primaryAxisAlignItems: "CENTER", counterAxisAlignItems: "CENTER" }); big.resize(64, 64);
  big.primaryAxisSizingMode = "FIXED"; big.counterAxisSizingMode = "FIXED"; big.cornerRadius = 32; big.fills = [P("soft")]; stroke(big, "selected");
  big.appendChild(await T("MC", "Heading", "blue")); h.appendChild(big);
  const w = col("Who", 2); h.appendChild(w); w.appendChild(await T("Maya Chen", "Heading")); w.appendChild(await T("@mayachen · Member since Nov 8, 2026", "Muted", "muted"));
}
async function account(y) {
  const sec = await section("Account", "Account", "F03 update profile · F05 change password · Sign out.", 6, y);
  let r, c, cd, a, grid;
  r = await screen(sec, 0, "AC1", "Profile", "F03 · Read-only profile with the same fields the Admin sees in Users › Profile.", "Account");
  c = content(r); await header(c, "Account", []); await profileHead(c);
  cd = await card(c, "Account details", 16);
  grid = F("Details", "HORIZONTAL", { layoutWrap: "WRAP", itemSpacing: 24, counterAxisSpacing: 16 }); put(cd, grid, "FILL");
  for (const [l, v] of [["Full name", "Maya Chen"], ["Username", "mayachen"], ["Email", "maya.chen@example.com"], ["Date of birth", "Jun 14, 2003"], ["Gender", "Female"], ["Address", "18 Nguyen Van Cu, Ho Chi Minh City"]]) {
    const i = await info(grid, l, v); fixW(i, 290);
  }
  a = row("Actions", 8); cd.appendChild(a); a.appendChild(B("Edit profile", "Primary", "edit")); a.appendChild(B("Change password", "Secondary", "key"));

  r = await screen(sec, 1, "AC2", "Edit profile", "F03 · Username can't be changed; address and birth date are optional.", "Account");
  c = content(r); await header(c, "Edit profile", [], "Account");
  cd = await card(c, null, 16);
  grid = F("Fields", "HORIZONTAL", { layoutWrap: "WRAP", itemSpacing: 16, counterAxisSpacing: 12 }); put(cd, grid, "FILL");
  for (const f of [field("Full name", "Maya Chen", "Focused"), field("Username", "mayachen", "Disabled", "Username can't be changed."), field("Email", "maya.chen@example.com"),
    field("Gender", "Female  ▾"), field("Date of birth (optional)", "2003-06-14"), field("Address (optional)", "18 Nguyen Van Cu, Ho Chi Minh City")]) { grid.appendChild(f); f.resize(440, f.height); }
  a = row("Actions", 8); a.primaryAxisAlignItems = "MAX"; put(cd, a, "FILL"); a.appendChild(B("Cancel", "Secondary")); a.appendChild(B("Save changes", "Primary"));

  r = await screen(sec, 2, "AC3", "Edit profile · errors", "F03 · Server validation: email already used, invalid birth date.", "Account");
  c = content(r); await header(c, "Edit profile", [], "Account");
  await banner(c, "error", "alert", "Fix the highlighted fields and save again.");
  cd = await card(c, null, 16);
  grid = F("Fields", "HORIZONTAL", { layoutWrap: "WRAP", itemSpacing: 16, counterAxisSpacing: 12 }); put(cd, grid, "FILL");
  for (const f of [field("Full name", "Maya Chen"), field("Username", "mayachen", "Disabled", "Username can't be changed."), field("Email", "noah.patel@example.com", "Error", "This email is already in use."),
    field("Gender", "Female  ▾"), field("Date of birth (optional)", "2031-02-30", "Error", "Enter a valid past date (YYYY-MM-DD)."), field("Address (optional)", "18 Nguyen Van Cu, Ho Chi Minh City")]) { grid.appendChild(f); f.resize(440, f.height); }
  a = row("Actions", 8); a.primaryAxisAlignItems = "MAX"; put(cd, a, "FILL"); a.appendChild(B("Cancel", "Secondary")); a.appendChild(B("Save changes", "Primary"));

  r = await screen(sec, 3, "AC4", "Change password", "F05 · Current password, new password, confirmation.", "Account");
  c = content(r); await header(c, "Change password", [], "Account");
  cd = await card(c, null, 14); fixW(cd, 520); cd.layoutSizingHorizontal = "FIXED";
  for (const f of [field("Current password", "••••••••"), field("New password", "••••••••••", "Focused", "At least 8 characters."), field("Confirm new password", "Re-enter the new password", "Placeholder")]) put(cd, f, "FILL");
  a = row("Actions", 8); a.primaryAxisAlignItems = "MAX"; put(cd, a, "FILL"); a.appendChild(B("Cancel", "Secondary")); a.appendChild(B("Update password", "Primary"));

  r = await screen(sec, 4, "AC5", "Change password · errors and success", "F05 · Wrong current password / mismatch errors; success banner after saving.", "Account");
  c = content(r); await header(c, "Change password", [], "Account");
  const two = row("States", 20); two.counterAxisAlignItems = "MIN"; put(c, two, "FILL");
  cd = col("Error state", 14); pad(cd, 16, 18); cd.fills = [P("white")]; stroke(cd, "line"); cd.cornerRadius = 8; two.appendChild(cd); cd.layoutSizingHorizontal = "FILL";
  for (const f of [field("Current password", "••••••", "Error", "Current password is incorrect."), field("New password", "••••••••••"), field("Confirm new password", "•••••••", "Error", "Passwords do not match.")]) put(cd, f, "FILL");
  put(cd, B("Update password", "Primary"), "FILL");
  const ok = col("Success state", 14); pad(ok, 16, 18); ok.fills = [P("white")]; stroke(ok, "line"); ok.cornerRadius = 8; two.appendChild(ok); ok.layoutSizingHorizontal = "FILL";
  await banner(ok, "ok", "check", "Password updated. Use your new password next time you sign in.");
  for (const f of [field("Current password", "Enter current password", "Placeholder"), field("New password", "Enter new password", "Placeholder"), field("Confirm new password", "Re-enter new password", "Placeholder")]) put(ok, f, "FILL");

  r = await screen(sec, 5, "AC6", "Sign out confirm", "Sign Out (sidebar) returns to the User Login.", "Account");
  c = content(r); await header(c, "Account", []); await profileHead(c);
  await dialog(r, "Sign out of ChatApp?", "You'll need your username and password to sign in again. Friends will see you as offline.", null, [B("Cancel", "Secondary"), B("Sign out", "Primary", "exit")]);
  return sec;
}

// ---------------------------------------------------------------- main
async function main() {
  await setup();
  const comp = await components(0);
  let y = comp.y + comp.height + 200;
  for (const build of [auth, chat, groups, friends, account]) {
    const s = await build(y);
    y = s.y + s.height + 200;
  }
  figma.viewport.scrollAndZoomIntoView(page.children.filter(n => n.name.startsWith(PREFIX)));
}

main().then(() => figma.closePlugin("ChatApp User UI generated: 42 screens + components."))
  .catch(e => { console.error(e); figma.closePlugin("Generator failed: " + (e && e.message ? e.message : e)); });
