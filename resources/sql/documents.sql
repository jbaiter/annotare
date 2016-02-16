-- name: create-document<!
-- creates a new document record
INSERT INTO documents
(name, project_id)
VALUES (:name, :project_id)

-- name: get-document
-- retrieve a document given the id.
SELECT * FROM documents
WHERE id = :id

-- name: get-documents
-- retrieve all documents.
SELECT * FROM documents

-- name: get-document-sentences
-- retrieve all sentences for a document
SELECT * FROM sentences WHERE document_id = :id

-- name: update-document!
-- update an existing document record
UPDATE documents
SET name = :name
WHERE id = :id

-- name: delete-document!
-- delete a document given the id
DELETE FROM documents
WHERE id = :id
