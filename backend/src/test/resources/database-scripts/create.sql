CREATE TABLE App_Role
(
    id          BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name        VARCHAR(50)  NOT NULL UNIQUE,
    description VARCHAR(250) NOT NULL
);

CREATE TABLE App_User
(
    id       BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    username VARCHAR(40)                     NOT NULL UNIQUE CHECK (length(username) >= 4),
    password CHAR(60)                        NOT NULL CHECK (length(password) = 60),
    email    VARCHAR(254)                    NOT NULL UNIQUE CHECK (length(email) >= 3),
    active   BOOLEAN                         NOT NULL DEFAULT (FALSE),
    banned   BOOLEAN                         NOT NULL DEFAULT (FALSE),
    role_id  BIGINT REFERENCES App_Role (id) NOT NULL
);

CREATE TABLE App_Category
(
    id          BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name        VARCHAR(50)  NOT NULL UNIQUE,
    description VARCHAR(250) NOT NULL,
    active      BOOLEAN      NOT NULL DEFAULT (TRUE)
);

CREATE TABLE App_Category_Role
(
    category_id BIGINT REFERENCES App_Category (id) ON DELETE CASCADE,
    role_id     BIGINT REFERENCES App_Role (id),
    PRIMARY KEY (category_id, role_id)
);

CREATE TABLE App_Thread
(
    id          BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    title       VARCHAR(80)                                           NOT NULL,
    create_date TIMESTAMP                                             NOT NULL DEFAULT (current_timestamp),
    active      BOOLEAN                                               NOT NULL DEFAULT (TRUE),
    pinned      BOOLEAN                                               NOT NULL DEFAULT (FALSE),
    category_id BIGINT REFERENCES App_Category (id) ON DELETE CASCADE NOT NULL,
    user_id     BIGINT REFERENCES App_User (id) ON DELETE CASCADE     NOT NULL
);

CREATE TABLE App_Post
(
    id          BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    content     VARCHAR(10000)                                      NOT NULL CHECK (length(content) >= 10),
    create_date TIMESTAMP                                           NOT NULL DEFAULT (current_timestamp),
    thread_id   BIGINT REFERENCES App_Thread (id) ON DELETE CASCADE NOT NULL,
    user_id     BIGINT REFERENCES App_User (id) ON DELETE CASCADE   NOT NULL
);
