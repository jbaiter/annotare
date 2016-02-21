-- name: create-project<!
-- creates a new project record
INSERT INTO projects
(name, description, tagset_id)
VALUES (:name, :description, :tagset_id)

-- name: get-project
-- retrieve a project given the id.
SELECT * FROM projects
WHERE id = :id

-- name: get-projects
-- retrieve all projects.
SELECT * FROM projects

-- name: get-project-documents
-- retrieve all documents for a project
SELECT id, name, project_id, COALESCE(c1, 0) AS sentence_count, COALESCE(c2, 0) AS untagged_count
    FROM documents D
    LEFT JOIN (SELECT document_id, COUNT(*) as c1
               FROM SENTENCES) AS S1 ON D.id = S1.document_id
    LEFT JOIN (SELECT document_id, num_edits, COUNT(*) as c2
               FROM SENTENCES WHERE num_edits = 0) AS S2 ON D.id = S2.document_id
    WHERE D.project_id = :id

-- name: get-untagged-sentences
-- retrieve random untagged sentences for the project
SELECT * FROM sentences
    WHERE document_id IN (SELECT id from documents WHERE project_id = :id)
          AND num_edits = 0
    ORDER BY RANDOM() LIMIT :limit;

-- name: update-project!
-- update an existing project record
UPDATE projects
SET name = :name, description = :description
WHERE id = :id

-- name: delete-project!
-- delete a project given the id
DELETE FROM projects
WHERE id = :id
