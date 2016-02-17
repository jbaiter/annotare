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

-- name: update-sentence!
-- update an existing sentence record
UPDATE sentences
SET tags = :tags, num_edits = num_edits + 1
WHERE id = :id

-- name: delete-sentence!
-- delete a sentence given the id
DELETE FROM sentences
WHERE id = :id
