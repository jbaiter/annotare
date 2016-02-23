-- name: create-document<!
-- creates a new document record
INSERT INTO documents
(name, project_id)
VALUES (:name, :project_id)

-- name: get-document
-- retrieve a document given the id.
SELECT id, name, project_id, COALESCE(c1, 0) AS sentence_count, COALESCE(c2, 0) AS untagged_count
    FROM documents D
    LEFT JOIN (SELECT document_id, COUNT(*) as c1
               FROM SENTENCES) AS S1 ON D.id = S1.document_id
    LEFT JOIN (SELECT document_id, num_edits, COUNT(*) as c2
               FROM SENTENCES WHERE num_edits = 0) AS S2 ON D.id = S2.document_id
    WHERE D.id = :id

-- name: get-documents
-- retrieve all documents.
SELECT id, name, project_id, COALESCE(c1, 0) AS sentence_count, COALESCE(c2, 0) AS untagged_count
    FROM documents D
    LEFT JOIN (SELECT document_id, COUNT(*) as c1
               FROM SENTENCES) AS S1 ON D.id = S1.document_id
    LEFT JOIN (SELECT document_id, num_edits, COUNT(*) as c2
               FROM SENTENCES WHERE num_edits = 0) AS S2 ON D.id = S2.document_id

-- name: get-document-sentences
-- retrieve all sentences for a document
SELECT * FROM sentences WHERE document_id = :id

-- name: get-project-documents
-- retrieve all documents for a project
SELECT id, name, project_id, COALESCE(c1, 0) AS sentence_count, COALESCE(c2, 0) AS untagged_count
    FROM documents D
    LEFT JOIN (SELECT document_id, COUNT(*) as c1
               FROM SENTENCES) AS S1 ON D.id = S1.document_id
    LEFT JOIN (SELECT document_id, num_edits, COUNT(*) as c2
               FROM SENTENCES WHERE num_edits = 0) AS S2 ON D.id = S2.document_id
    WHERE D.project_id = :id

-- name: update-document!
-- update an existing document record
UPDATE documents
SET name = :name
WHERE id = :id

-- name: delete-document!
-- delete a document given the id
DELETE FROM documents
WHERE id = :id
