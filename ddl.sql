CREATE TABLE articles (
  article_id SERIAL PRIMARY KEY,
  title TEXT,
  content TEXT,
  is_searchable BOOLEAN,
  tags TEXT,
  update_date TIMESTAMP,
  create_date TIMESTAMP
)
 -- tags
CREATE TABLE tags (
  tag_id SERIAL PRIMARY KEY,
  tag_name VARCHAR(100)
);

CREATE TABLE articles_tags (
  tag_id BIGINT,
  article_id BIGINT,
  PRIMARY KEY(tag_id, article_id)
);

CREATE INDEX idx_articles_tags_article_id
ON articles_tags(article_id);
CREATE INDEX idx_articles_tags_tag_id
ON articles_tags(tag_id);

CREATE TABLE users (
  user_id SERIAL PRIMARY KEY,
  login VARCHAR(60),
  password VARCHAR(64),
  role VARCHAR(100)
)

CREATE TABLE user_rights (
  role VARCHAR(100) PRIMARY KEY
)



