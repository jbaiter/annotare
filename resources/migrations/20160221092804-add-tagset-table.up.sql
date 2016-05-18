CREATE TABLE tagsets (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    name            VARCHAR(512) NOT NULL UNIQUE,
    tags            TEXT NOT NULL,
    empty_tag       VARCHAR(32) NOT NULL,
    documentation   TEXT
);
--;;
INSERT INTO tagsets (name, tags, empty_tag)
    SELECT name || " (Tags)", tagset, empty_tag FROM projects;
--;;
ALTER TABLE projects RENAME TO projects_old;
--;;
CREATE TABLE projects(
  id          INTEGER PRIMARY KEY AUTOINCREMENT,
  name        VARCHAR(512) NOT NULL UNIQUE,
  tagset_id   INTEGER NOT NULL,
  description TEXT,
  FOREIGN KEY(tagset_id) REFERENCES tagsets(id)
);
--;;
INSERT INTO projects (id, name, description, tagset_id)
    SELECT p.id, p.name, description, t.id AS tagset_id FROM projects_old AS p
    JOIN tagsets AS t ON p.tagset=t.tags AND p.empty_tag = t.empty_tag;
--;;
DROP TABLE projects_old;
