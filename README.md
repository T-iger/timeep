#使用事项
请求字段说明
序号	参数名	参数类型	说明
1	flag	Int	查询的种类
1->教育属性
2->教材体系
3->知识点体系
4->知识图谱
2	relation	String	1、教育属性：query为空查所有，relation为all
2、教材体系：query为空查所有，relation为all，query传教材版本时查单条，relation为single
3、知识点体系：query传的是学段学科，relation为all，（缺少难度），query为空查所有，relation为all
4、query为空查所有，relation为all
3	query	String	查询的内容，query为空时就是不传


请求报文样例：
以下参数为form-data格式
教育属性查询-报文样例
{
	"flag": ”1”, 
	"relation":” all”,
"query":””
}
教材体系查询（单个版本查询）
{
	"flag": ”2”, 
	"relation":” single”,
"query":” MathBookHK2014Chuzhong”
}
教材体系查询（查询所有版本）
{
	"flag": ”2”, 
	"relation":” all”,
"query":””
}
知识点体系查询（查询单个学科学段）
{
	"flag": ”3”, 
	"relation":” all”,
"query":” MathKChuzhong”
}
知识点体系查询（查询所有学科学段）
{
	"flag": ”3”, 
	"relation":” all”,
"query":””
}
知识图谱查询
{
	"flag": ”3”, 
	"relation":” all”,
"query":””
}
##1.1.3.	返回参数
返回样例报文：
{"NOTE":"[{name:'Thing',des:'Thing',symbolSize:60,category:0},
		{name:'高中',des:'高中',symbolSize:60,category:1},
		{name:'高一年级',des:'高一年级',symbolSize:40,category:2},
		{name:'高三年级',des:'高三年级',symbolSize:40,category:2},
		{name:'高二年级',des:'高二年级',symbolSize:40,category:2},
		{name:'高中学段',des:'高中学段',symbolSize:40,category:2},
		{name:'十二年一贯',des:'十二年一贯',symbolSize:60,category:1},
		{name:'初中学段',des:'初中学段',symbolSize:40,category:2}]",
"LINK":"[{source:'Thing',target:'高中',name:' '},
		{source:'高中',target:'高一年级',name:' '},
		{source:'高中',target:'高三年级',name:' '},
		{source:'高中',target:'高二年级',name:' '},
		{source:'高中',target:'高中学段',name:' '},
		{source:'Thing',target:'十二年一贯',name:' '},
		{source:'十二年一贯',target:'高二年级',name:' '},
		{source:'十二年一贯',target:'高中学段',name:' '}]"
}
响应参数说明：
NOTE是构图结点数据
LINK是关系数据
若查询失败返回false
#1.2.	知识图谱执行同步接口 
##1.2.1.	接口说明
该接口用来请求同步（请勿在一段时间内多次提交执行更新请求，执行更新需要一定时间）
##1.2.2.	请求参数
请求字段说明
序号	参数名	参数类型	说明
1	time	Date	操作时间
2	do	Int	执行更新（1）
3	version	String	更新版本
请求报文样例：
{ 
	"time":”2020/06/01 15:08:55”,
  "do":”1” 
  "version":”1”
}
##1.2.3.	返回参数
返回样例报文：
{
	" time":"2020/06/01 15:18:55",
	" do":"1"
	" version ":"1"
}
响应参数说明：
time				返回时间
do				是否更新成功
version			更新的版本
#1.3.	知识图谱数据同步接口 
该接口文档另存
#1.4.	列表数据查询接口
##1.4.1.	接口说明
该接口用来查询列表数据（拥有层数控制、查询前后序、参考知识点）
序号	参数名	参数类型	说明
1	knowledge	String	查询的知识点
2	number	Int	查询的层数
	
请求报文样例
以下参数为form-data格式
{
	" knowledge ":"有理数的运算律",
	" number ":"1"
}
返回数据样例
{
    "NOTE": "[{name:'有理数运算律',des:'有理数运算律',symbolSize:40,category:0},{name:'有理数的加、减、乘、除、乘方及简单的混合运算',des:'有理数的加、减、乘、除、乘方及简单的混合运算',symbolSize:50,category:1},{name:'乘方',des:'乘方',symbolSize:50,category:1},{name:'绝对值',des:'绝对值',symbolSize:50,category:1},{name:'相反数',des:'相反数',symbolSize:50,category:1},{name:'数轴',des:'数轴',symbolSize:50,category:1}]",
    "LINK": "[{source:'有理数运算律',target:'有理数的加、减、乘、除、乘方及简单的混合运算',name:'后继知识点'},{source:'有理数运算律',target:'乘方',name:'前序知识点'},{source:'乘方',target:'绝对值',name:'前序知识点'},{source:'绝对值',target:'相反数',name:'前序知识点'},{source:'相反数',target:'数轴',name:'前序知识点'},{source:'乘方',target:'有理数运算律',name:'参考知识点',lineStyle: {normal: { curveness: 0.3 }}}]"
}
