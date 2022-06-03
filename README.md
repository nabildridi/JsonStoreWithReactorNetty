# A Json Store with Reactor Netty

Exactly the same as  [(JsonStoreWithVertx)](https://github.com/nabildridi/JsonStoreWithVertx) but done with reactor-netty


## Some benchmarks :

Test : 33000 documents fully cached, I7 processor with 16 gb of ram

|Operation| Time |
|--|--|
| Pagination |  ~3 ms |
| Pagination and sorting | ~30 ms |
| Pagination and extracting  | ~3 ms |
| Pagination, sorting and extracting  | ~23 ms |
| Pagination and filtering  | ~11 ms |
| Pagination, filtering, sorting and extracting | ~12 ms |




