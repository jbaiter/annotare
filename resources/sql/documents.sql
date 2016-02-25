-- name: create-document<!
-- creates a new document record
INSERT INTO documents
(name, project_id)
VALUES (:name, :project_id)

-- name: get-document
-- retrieve a document given the id.
SELECT d.id, d.name, d.project_id, COUNT(s1.id) AS sentence_count,
       COUNT(s2.id) as untagged_count FROM documents d
    LEFT JOIN sentences AS s1 ON d.id = s1.document_id
    LEFT JOIN sentences AS s2 ON d.id = s2.document_id AND s1.id = s2.id
        AND s2.num_edits = 0
    WHERE d.id = :id GROUP BY d.id

-- name: get-documents
-- retrieve all documents.
SELECT d.id, d.name, d.project_id, COUNT(s1.id) AS sentence_count,
       COUNT(s2.id) as untagged_count FROM documents d
    LEFT JOIN sentences AS s1 ON d.id = s1.document_id
    LEFT JOIN sentences AS s2 ON d.id = s2.document_id AND s1.id = s2.id
        AND s2.num_edits = 0
    GROUP BY d.id

-- name: get-document-sentences
-- retrieve all sentences for a document
SELECT * FROM sentences WHERE document_id = :id

-- name: get-project-documents
-- retrieve all documents for a project
SELECT d.id, d.name, d.project_id, COUNT(s1.id) AS sentence_count,
       COUNT(s2.id) as untagged_count FROM documents d
    LEFT JOIN sentences AS s1 ON d.id = s1.document_id
    LEFT JOIN sentences AS s2 ON d.id = s2.document_id AND s1.id = s2.id
        AND s2.num_edits = 0
    WHERE d.project_id = :id GROUP BY d.id

-- name: update-document!
-- update an existing document record
UPDATE documents
SET name = :name
WHERE id = :id

-- name: delete-document!
-- delete a document given the id
DELETE FROM documents
WHERE id = :id
