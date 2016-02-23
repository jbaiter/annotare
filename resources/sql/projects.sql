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
