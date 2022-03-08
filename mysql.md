# 建库示例
CREATE DATABASE IF NOT EXISTS xxx CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci; 

general_ci、unicode_ci：排序规则，中英文没有差别，一般用general_ci即可
- general_ci：校对速度更快，但准确度更低
- unicode_ci：校对速校对度更慢，但准确度更高，法文和德文一定要使用unicode_ci

ci：case insensitive，大小写不敏感，不区分大小写 
cs：大小写敏感，区分大小写 
bin：区分大小写，每个字符串用二进制数据编译存储，例如：utf8_bin 






