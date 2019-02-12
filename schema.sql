CREATE DATABASE todolist;
USE todolist;

CREATE TABLE task (
  task_id INTEGER PRIMARY KEY AUTO_INCREMENT,
  task_label VARCHAR(500) NOT NULL,
  task_createdate DATETIME NOT NULL DEFAULT NOW(),
  task_duedate DATETIME NOT NULL,
  task_isCompleted BOOLEAN NOT NULL DEFAULT FALSE,
  task_isCancelled BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE tag (
  task_id INTEGER,
  tag_name VARCHAR(500) NOT NULL,
  FOREIGN KEY (task_id) REFERENCES task(task_id)
);