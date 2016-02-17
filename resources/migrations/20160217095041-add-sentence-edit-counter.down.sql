ALTER TABLE sentences RENAME TO sentences_old;
--;;
CREATE TABLE sentences (
id      INTEGER PRIMARY KEY AUTOINCREMENT,
tokens  TEXT,
tags    TEXT,
document_id INTEGER NOT NULL,
FOREIGN KEY(document_id) REFERENCES documents(id)
);
--;;
INSERT INTO sentences (id, tokens, tags, document_id)
    SELECT id, tokens, tags, document_id FROM sentences_old;
--;;
DROP TABLE sentences_old;
