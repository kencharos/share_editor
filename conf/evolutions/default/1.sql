# Users スキーマ
 
# --- !Ups
 
CREATE TABLE Theme (
    theme varchar(255) NOT NULL ,
    content varchar(2000) NOT NULL ,
    PRIMARY KEY (theme)
);

INSERT INTO Theme values ('Sample.java', '//好きに打て');

CREATE TABLE Answer (
    theme varchar(255) NOT NULL ,
    name varchar(30) NOT NULL ,
    content varchar(2000) NOT NULL ,
    PRIMARY KEY (theme,name)
);
