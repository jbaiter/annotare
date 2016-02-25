CREATE TABLE projects (
  id          INTEGER PRIMARY KEY AUTOINCREMENT,
  name        VARCHAR(512) NOT NULL UNIQUE,
  tagset      TEXT NOT NULL,
  empty_tag   VARCHAR(32) NOT NULL,
  description TEXT
);
--;;
CREATE TABLE documents (
  id    INTEGER PRIMARY KEY AUTOINCREMENT,
  name  VARCHAR(512),
  project_id INTEGER NOT NULL,
  FOREIGN KEY(project_id) REFERENCES projects(id) ON DELETE CASCADE
);
--;;
CREATE TABLE sentences (
  id      INTEGER PRIMARY KEY AUTOINCREMENT,
  tokens  TEXT,
  tags    TEXT,
  document_id INTEGER NOT NULL,
  FOREIGN KEY(document_id) REFERENCES documents(id) ON DELETE CASCADE
);
