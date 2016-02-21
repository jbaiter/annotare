-- name: create-tagset<!
-- creates a new tagset record
INSERT INTO tagsets (name, tags, empty_tag, documentation)
    VALUES (:name, :tags, :empty_tag, :documentation);

-- name: get-tagset
-- retrieve a tagset given the id
SELECT * FROM tagsets WHERE id = :id

-- name: get-tagsets
-- retreive all tagsets
SELECT * FROM tagsets

-- name: update-tagset!
-- update a given tagset
UPDATE tagsets
    SET name = :name, documentation = :documentation, tags = :tags
    WHERE id = :id

-- name:  delete-tagset!
-- delete a given tagset
DELETE FROM tagsets WHERE id = :id
