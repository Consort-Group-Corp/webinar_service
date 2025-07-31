CREATE TABLE IF NOT EXISTS webinar_schema.webinar_participants (
    id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    webinar_id UUID NOT NULL,
    user_id UUID NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT uq_webinar_user UNIQUE (webinar_id, user_id),
    CONSTRAINT fk_webinar FOREIGN KEY (webinar_id)
    REFERENCES webinar_schema.webinars (id) ON DELETE CASCADE
);