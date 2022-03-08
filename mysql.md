# 建库示例
```sql
```
CREATE DATABASE IF NOT EXISTS xxx CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci; 

COLLATE：校对规则，翻译为排序规则不准确，影响查询校对和排序，例如等号查询'='  
general_ci、unicode_ci：中英文没有差别，一般用general_ci即可
- general_ci：校对速度更快，但准确度更低
- unicode_ci：校对速校对度更慢，但准确度更高，法文和德文一定要使用unicode_ci

ci：case insensitive，大小写不敏感，不区分大小写  
cs：大小写敏感，区分大小写  
bin：区分大小写，每个字符串用二进制数据编译存储，例如：utf8_bin 

注意：有些mysql版本不支持cs编码，可以考虑使用bin规则

##  大小写敏感查询

**查询where指定BiNARY**
```sql
select * from tb_user where BINARY username ='user';
```






