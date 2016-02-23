-- name: create-tagset<!
-- creates a new tagset record
INSERT INTO tagsets (name, tags, empty_tag, documentation)
    VALUES (:name, :tags, :empty_tag, :documentation)

-- name: get-tagset
-- retrieve a tagset given the id
SELECT * FROM tagsets WHERE id = :id

-- name: get-tagsets
-- retreive all tagsets
SELECT * FROM tagsets

-- :name get-sentence-tagset :? :1
-- :doc retrieve the tagset for a sentence
SELECT t.id, t.tags, t.empty_tag, t.documentation FROM tagsets t
    JOIN projects AS p ON p.tagset_id = t.id
    JOIN documents AS d ON d.project_id = p.id
    JOIN sentences AS s ON s.document_id = d.id AND s.id = :id

-- :name get-document-tagset :? :1
-- :doc retrieve the tagset for a sentence
SELECT t.id, t.tags, t.empty_tag, t.documentation FROM tagsets t
    JOIN projects AS p ON p.tagset_id = t.id
    JOIN documents AS d ON d.project_id = p.id AND d.id = :id

-- :name get-project-tagset :? :1
-- :doc retrieve the tagset for a sentence
SELECT t.id, t.tags, t.empty_tag, t.documentation FROM tagsets t
    JOIN projects AS p ON p.tagset_id = t.id AND p.id = :id

-- name: update-tagset!
-- update a given tagset
UPDATE tagsets
    SET name = :name, documentation = :documentation, tags = :tags
    WHERE id = :id

-- name:  delete-tagset!
-- delete a given tagset
DELETE FROM tagsets WHERE id = :id
