-- name: create-project<!
-- creates a new project record
INSERT INTO projects
(name, tagset, empty_tag, description)
VALUES (:name, :tagset, :empty_tag, :description)

-- name: get-project
-- retrieve a project given the id.
SELECT * FROM projects
WHERE id = :id

-- name: get-projects
-- retrieve all projects.
SELECT * FROM projects

-- name: get-project-documents
-- retrieve all documents for a project
SELECT * FROM documents WHERE project_id = :id

-- name: update-project!
-- update an existing project record
UPDATE projects
SET name = :name, description = :description
WHERE id = :id

-- name: delete-project!
-- delete a project given the id
DELETE FROM projects
WHERE id = :id
