create table cache
(
	id int auto_increment,
	name varchar(23) null,
	type int null,
	age int null,
	constraint cache_pk
		primary key (id)
);


insert into cache values (1,'cacheName1',3,24);
insert into cache values (2,'cacheName2',3,24);
insert into cache values (3,'cacheName3',3,24);
insert into cache values (4,'cacheName4',3,24);
insert into cache values (5,'cacheName5',3,24);
insert into cache values (6,'cacheName6',3,24);
