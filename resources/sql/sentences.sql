-- name: create-sentence<!
-- creates a new sentence record
INSERT INTO sentences
(tokens, tags, document_id)
VALUES (:tokens, :tags, :document_id)

-- name: get-sentence
-- retrieve a sentence given the id.
SELECT * FROM sentences
WHERE id = :id

-- name: get-sentences
-- retrieve all sentences.
SELECT * FROM sentences

-- name: get-sentence-tagset
-- retrieve the tagset for a sentence
SELECT t.id, t.tags, t.empty_tag, t.documentation FROM tagsets t
    JOIN projects AS p ON p.tagset_id = t.id
    JOIN documents AS d ON d.project_id = p.id
    JOIN sentences AS s ON s.document_id = d.id AND s.id = :id

-- name: update-sentence!
-- update an existing sentence record
UPDATE sentences
SET tags = :tags, num_edits = num_edits + 1
WHERE id = :id

-- name: delete-sentence!
-- delete a sentence given the id
DELETE FROM sentences
WHERE id = :id
