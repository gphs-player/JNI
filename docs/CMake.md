## CMake入门



### 一、CMake基本语法

#### 1.单目录单源文件

目录`cmake/`下写一个`main.cpp`文件，内容就是打印一行LOG。这次让CMAKE来帮忙生成一个可执行文件

```c++
#include<iostream>
int main(){
 		using namespace std;
    std::cout << "Main Test ...\n";
    return 0;   
}
```

在`cmake/`创建`CmakeLists.txt`文件，内容如下

```cmake
project(HelloDemo)##项目名称是HelloDemo

add_executable(main main.cpp)##生成的可执行文件名是main  main.cpp作为源文件输入
```

本着`out-of-source`的原则，在cmake/目录下新建个build目录，然后依次执行

```shell
cmake ..
make
```

make命令要在MakeFile生成之后执行。

然后会在我们的build目录下生成main可执行文件，自行测试。



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

```cmake
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

#### 3.多目录结构

进一步探索多目录的结构，试想你的项目源代码在SRC目录，第三方依赖库在LIBS目录，这时候cmake的配置该如何呢？

```
➜  multiDir git:(master) ✗ tree
.
├── CmakeLists.txt
├── build 
├── deps
│   └── welcome.cpp
└── main
    └── main.cpp
```

如上所示，main目录用于存放自己工程源文件，deps目录存放依赖库，build目录用于编译。

拿到这么一个目录怎么去管理编译过程呢？怎么写CmakeLists.txt？

我大概会想：把所有目录都添加到编译范围，然后一起编译。来试一下：

```cmake
project(Cmake)
#把所有目录都添加进工程
aux_source_directory(main SOURCE)
aux_source_directory(deps LIBS)

add_executable(welcome ${SOURCE} ${LIBS})
```

看下编译过程：

```
➜  build git:(master) ✗ cmake ..
-- The C compiler identification is AppleClang 11.0.0.11000033
....省略部分输出
-- Generating done
-- Build files have been written to: /Users/xxx/multiDir/build
➜  build git:(master) ✗ make
Scanning dependencies of target welcome
[ 33%] Building CXX object CMakeFiles/welcome.dir/main/main.o
[ 66%] Building CXX object CMakeFiles/welcome.dir/deps/welcome.o
[100%] Linking CXX executable welcome
[100%] Built target welcome
```

在build目录下顺利的看到了welcome的输出文件。

到这里是不是能告一段落了？

但似乎也不对啊！我们在Android开发的时候，会看到很多so的文件啊，怎么生成so才是重点吧。

结合上一篇[C/C++编译链接库](./BuildLib.md)，继续来看下Cmake如何编译出链接库文件。



### 二、CMake编译链接库

在Cmake构建系统中，项目根目录下会有一个`CmakeLists.txt`文件，用于配置编译选项及依赖项，如果存在多个子目录，通常也会在子目录下各自新建一个`CmakeLists.txt`文件，用于配置当前目录的编译选项。所以，我们刚才的Demo虽说是OK的，但是并不是合适的做法。

修改一下主工程的`CmakeLists.txt`:

```cmake
project(Cmake)

add_subdirectory(main)

add_subdirectory(deps)
```

简单点，直接添加所需目录，剩下的编译选项在各自目录的`CmakeLists.txt`进行配置。



然后在本例中deps目录就是我们的依赖库，应该把它编译成为一个静态库或者共享库：来写deps下的`CmakeLists.txt`。

```cmake
aux_source_directory(. MODULE)

add_library(WELCOMELIB ${MODULE}) # 语法add_library(VAR DIR)
```

两行的内容就是将当前目录的代码编译出一个名字叫做`WELCOMELIB`的library，以备使用。

最后是main目录下的配置，这里就必须要有程序的入口了：

```
aux_source_directory(. SOURCE)

add_executable(welcome ${SOURCE})

#添加链接库
target_link_libraries(welcome WELCOMELIB)
```

就是多了最后一行，把一个名字叫做`WELCOMELIB`的库链接到主程序中。该语法用于在运行时导入某个库。

再来编译看结果：

```
➜  build git:(master) ✗ cmake ..
...省略输出
➜  build git:(master) ✗ make
Scanning dependencies of target WELCOMELIB
[ 25%] Building CXX object deps/CMakeFiles/WELCOMELIB.dir/welcome.o
[ 50%] Linking CXX static library libWELCOMELIB.a
[ 50%] Built target WELCOMELIB
Scanning dependencies of target welcome
[ 75%] Building CXX object main/CMakeFiles/welcome.dir/main.o
[100%] Linking CXX executable welcome
[100%] Built target welcome

```

注意这行

`[ 50%] Linking CXX static library libWELCOMELIB.a`

确实生成了一个`libWELCOMELIB.a`的库，命名方式也是lib[xxx].a,这就是我们想要的链接库了，只是它是一个静态库。怎么改为共享库呢？

修改下deps下的`Cmakelists.txt`:

```cmake
aux_source_directory(. MODULE)

add_library(WELCOMELIB SHARED ${MODULE})
```

新增一个`SHARED`属性，再看编译输出结果：

```
➜  build git:(master) ✗ make
Scanning dependencies of target WELCOMELIB
[ 25%] Building CXX object deps/CMakeFiles/WELCOMELIB.dir/welcome.o
[ 50%] Linking CXX shared library libWELCOMELIB.dylib
[ 50%] Built target WELCOMELIB
[ 75%] Linking CXX executable welcome
[100%] Built target welcome

```

这次的输出就是`[ 50%] Linking CXX shared library libWELCOMELIB.dylib`,在MAC下.dylib就是我们想要的so库。

`add_library()`默认生成一个`STATIC`的链接库，除非我们自己指定链接库的类型。语法如下：

```
add_library(archive SHARED archive.cpp zip.cpp lzma.cpp)
add_library(archive STATIC archive.cpp zip.cpp lzma.cpp)
```

目前为止，Cmake如何构建一个项目，我们有了一个大概的认知。接下来继续探索，Cmake如何使用别人提供给你的.so/.a库。

### 三、测试链接库

在测试链接so库之前，我们先创建一个动态库，新建buildLib目录和相关文件

```
➜  buildLib git:(master) ✗ tree
├── CmakeLists.txt
├── build
└── lib
    ├── CmakeLists.txt
    ├── welcome.cpp
    └── welcome.h
```

lib下的Cmakelists.txt配置如下，创建一个WELCOMELIB的共享库

```
aux_source_directory(. MODULE)

add_library(WELCOMELIB SHARED ${MODULE})
```

头文件内容如下：

```C++
#ifndef JNI_WELCOME_H
#define JNI_WELCOME_H
#include<iostream>
void welcome();
#endif //JNI_WELCOME_H
```

编译过程和之前一致，在build目录下生成了`libWELCOMELIB.dylib`的共享库

接下来就是如何使用：

新建testLib目录，准备好测试代码:

```
➜  testLib git:(master) ✗ tree
├── CmakeLists.txt
├── build
├── funs
│   └── welcome.h
├── libs
│   └── libWELCOMELIB.dylib
└── main.cpp

```

1.工程下新建main.cpp测试代码

2.将`libWELCOMELIB.dylib`拷贝至libs目录下

3.用到的头文件拷贝到funs目录下（也可以在创建共享库的时候INSTALL到指定位置，参考文末（CMake实践笔记））

然后是测试代码：

```C++
#include<iostream>
#include <welcome.h>

int main(){
    std::cout <<"Test Start... \n";
    welcome();
    std::cout <<"Test END\n";
    return 0;
}
```

CmakeLists.txt如下

```cmake
project(HelloLib)

ADD_EXECUTABLE(main main.cpp)
```

测试下

```shell
➜  build git:(master) ✗ make
Scanning dependencies of target main
[ 50%] Building CXX object CMakeFiles/main.dir/main.cpp.o
/Users/lihua/Learn/Android/JNI/cmake/testLib/main.cpp:6:10: fatal error: 'welcome.h' file not found
#include <welcome.h>
         ^~~~~~~~~~~
1 error generated.
make[2]: *** [CMakeFiles/main.dir/main.cpp.o] Error 1
make[1]: *** [CMakeFiles/main.dir/all] Error 2
make: *** [all] Error 2
```

直接报错，找不到`welcome.h`，虽然这个头文件在工程下，但是并未纳入编译的范围内，怎么做呢？

第一种做法是 `#include "funs/welcome.h"`,没错，简单又直接。

第二种做法是通过CMake找到这个包含头文件的目录，让它自动参与编译，在CmakeLists.txt下添加如下：

```cmake
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/funs)
```

内置变量`CMAKE_CURRENT_SOURCE_DIR`是当前主工程的绝对路径，更多内置变量参考文末链接，在使用之前可以使用`MESSAGE()`方法LOG一下。

再次编译：

```shell
headerpad_max_install_names  CMakeFiles/main.dir/main.o  -o main 
Undefined symbols for architecture x86_64:
  "welcome()", referenced from:
      _main in main.o
ld: symbol(s) not found for architecture x86_64
clang: error: linker command failed with exit code 1 (use -v to see invocation)
make[2]: *** [main] Error 1
make[1]: *** [CMakeFiles/main.dir/all] Error 2
make: *** [all] Error 2
```

刚才的错误没有了，但是又有新的错误出现，我们的共享库并没有被连接到主程序。继续添加如下：

```cmake
target_link_libraries(main ${CMAKE_CURRENT_SOURCE_DIR}/libs/libWELCOMELIB.dylib)
```

将指定的库添加到编译过程中。

整个`CmakeLists.txt`文件内容如下

```cmake
project(HelloLib)

ADD_EXECUTABLE(main main.cpp)

include_directories(${CMAKE_CURRENT_SOURCE_DIR}/funs)

target_link_libraries(main ${CMAKE_CURRENT_SOURCE_DIR}/libs/libWELCOMELIB.dylib)
```

再次编译：

```shell
➜  build git:(master) ✗ make
Scanning dependencies of target main
[ 50%] Building CXX object CMakeFiles/main.dir/main.o
[100%] Linking CXX executable main
[100%] Built target main
➜  build git:(master) ✗ ./main 
Test Start... 
Welcome to C++Lib...
Test END
```

正常编译，看到想要的结果。



### 参考

[CMake内置变量](https://cmake.org/cmake/help/v3.16/manual/cmake-variables.7.html)

[《CMake实践》笔记](https://www.cnblogs.com/52php/p/5681755.html)

[CMake 入门实战](https://www.hahack.com/codes/cmake/)

