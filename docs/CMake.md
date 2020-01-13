## CMake入门



### 一、CMake基本语法

#### 1.单目录单源文件

目录`cmake/`下写一个`main.cpp`文件，就是打印一行LOG。让CMAKE来帮忙生成一个可执行文件

```c++
#include<iostream>
int main(){
 		using namespace std;
    std::cout << "Main Test ...\n";
    return 0;   
}
```

在`cmake/`创建`CmakeLists.txt`文件，内容如下

```shell
project(HelloDemo)##项目名称是HelloDemo

add_executable(main main.cpp)##生成的可执行文件名是main  main.cpp作为源文件输入
```

本着out-of-source的原则，在cmake/目录下新建个build目录，然后依次执行

```shell
cmake ..
make
```

make命令要在MakeFile生成之后执行。

最终会在我们的build目录下生成main可执行文件。



#### 2.单目录多源文件结构

目录下应该会有多个源文件，怎么统一把它们添加到编译范围里？

`add_executable(welcome main.cpp welcome.cpp xxx.cpp xxx.cpp)`???

上述写法也能实现，但是Cmake提供了语法支持。

`aux_source_directory(<dir> <variable>)`

上述语法用于将某个目录下的文件添加到编译范围，并存入指定的变量。

举例，我们把打印LOG的语法抽出到另外一个文件`welcome.cpp`。

```c++
#include<iostream>
void welcome(){
    std::cout <<"Welcome to C++..."<<std::endl;
}
```

main.cpp如下

```c++
#include<iostream>
void welcome();
int main(){
    using namespace std;
    welcome();
    return 0;   
}
```

那么cmakelist的语法就对应的修改为：

```
project(Cmake)

aux_source_directory(. SOURCE) ###我们定义的变量名字为SOURCE

add_executable(welcome ${SOURCE}) ### 引用变量采用${VAR}格式   welcome是可执行程序的名称
```

再次在build目录下执行

```shell
cmake..
make
```

执行welcome程序

```shell
➜  build ✗ ./welcome 
Welcome to C++...
```







### 二、CMake编译链接库



### 三、测试链接库