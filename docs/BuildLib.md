## C/C++的输出和链接库                              

### 一、编译命令与文件格式

* `.out`等可执行文件

  一个简单的`helloWorld.c`源文件,使用命令`gcc helloWorld.c` 会输出`a.out`可执行文件,或者`gcc -o hello helloWorld.c` 生成`hello`可执行文件。

* `.o`文件,每个`.c`源文件生成的非可执行文件,用于链接到主程序生成可执行文件,可以通过`objdump -d helloWorld.o`命令查看对应的内容。

  ``gcc -c helloWorld.c`` 会生成`helloWorld.o`文件

* `.s`文件,是`helloWorld.c`对应的汇编语言文本格式，可直接查看。

  `gcc -S helloWorld.c` 会生成`helloWorld.s`文件。

* `.a`静态库,应用程序会将静态库一起打包。10个应用会打包10份静态库，每个应用的包都会增大。

* `.so`动态库，依赖同一个库的应用程序会共享这个库的副本。

对于链接库来说，分为静态和动态库两种，要使用它们，必须包含对应的include头文件。链接库的命令方式一般是-L(指定库的绝对路径)和-l（指定库名称），假如有一个`libmine.so`的库需要链接到`myprog.c`主程序，那么命令应该像下面这样：

`$ gcc -o myprog myprog.c  -L/home/newhall/lib -lmine` 

myprog就是最终生成的可执行程序的名称。

### 二、链接.o文件

一个源文件可以生成一个.o文件，也可以直接将若干个.o文件直接链接到主程序。比如：

`gcc test.c other.o mylib.o`

那么默认生成的还是a.out输出，但是会把对应的.o文件打包到一起。

### 三、静态链接库

#### 1.生成.o文件 

假设我们有如下目录结构的文件，`static`目录下是需要打包为library的文件。

```shell
├── main.cpp
└── static ///静态库的文件
    ├── algo.cpp
    ├── algo.h
    ├── logger.cpp
    └── logger.h
```

首先需要把`static`目录下的源文件打包为`.o`文件。在`static`目录执行以下命令：

`➜  g++ -c *.cpp`

目录下新增两个`.o`文件

```shell
.
├── algo.cpp
├── algo.h
├── algo.o
├── logger.cpp
├── logger.h
└── logger.o
```

然后准备打包`.a`静态链接库

#### 2.打包.o生成.a静态库

还是在`static`目录下执行`ar -cvq libutil.a *.o` 将`.o`文件打包生成`libutil.a`静态库，命名方式为lib[XXX].a,。
-v 提供详细输出
-q 快速创建文档

```
➜  ar -cvq libutil.a *.o
q - algo.o
q - logger.o
➜  tree
.
├── algo.cpp
├── algo.h
├── algo.o
├── libutil.a
├── logger.cpp
├── logger.h
└── logger.o
```

##### 查看静态库组成

通过如下命令查看`.a`库的组成。
```
➜  ar -t static/libutil.a 
__.SYMDEF SORTED
algo.o
logger.o
```
#### 3.静态库链接

接下来回到主程序目录，要把静态库链接到我们的主程序了，可以使用下面两个方法：

`$ g++ -o main main.cpp ./static/libutil.a `///指定库的绝对路径
或者
`$ g++ -o main main.cpp -L./static -lutil ` /// -L指定相对目录，-l指定库的名字
在当前目录下会生成`main`的可执行文件。



### 四、动态链接库

#### 文件目录结构

foo.h

```C
#ifndef CMAKE_FOO_H
#define CMAKE_FOO_H
extern void foo(void);
#endif 
```

foo.c

```c
#include <stdio.h>
void foo(){
    printf("Hello, I an a shared lib");
}
```

main.c

```c
#include <stdio.h>
#include "foo.h"

int main(){
    printf("开始测试动态链接库...\n");
    foo();
    printf("\n测试动态链接库结束\n");
    return 0;
}
```

#### 1.生成.o文件

还是编译独立文件,在当前目录下生成`foo.o`文件

`gcc -c -Wall -Werror -fpic foo.c`

#### 2.生成动态库.so文件

命名格式也是lib[xxx].so

` gcc -shared -o libfoo.so foo.o `

#### 3.将动态库链接到主程序

```shell
gcc -Wall -o test main.c -lfoo
ld: library not found for -lfoo
clang: error: linker command failed with exit code 1 (use -v to see invocation)
```

库找不到???!!!

因为链接器不知道去哪加载so库，gcc有一个默认的地址去加载对应的库，但是我们新生成的库并不在这个默认地址下。需要显示的告诉GCC，来我这里加载。这就需要`-L`选项

`gcc -L/Users/xxx/Cmake/shared -Wall -o test main.c -lfoo`

ok,一个名字叫test的可执行程序就生成了。

到目前为止，Demo就结束了。

如果存在其他问题参考[Shared Libraries](https://www.cprogramming.com/tutorial/shared-libraries-linux-gcc.html)

### 注意

1.在将.so/.a文件链接到主程序的时候，可选增加 -o选项指定输出文件名。默认还是a.out输出。

2.链接动态库的时候会有一个环境变量`LD_LIBRARY_PATH`,但是Demo中只用到了-L绝对路径来测试。

3.每个Demo的目录结构是不一致的。

### 参考

[C Libraries](https://www.cs.swarthmore.edu/~newhall/unixhelp/howto_C_libraries.html)

[https://www.cnblogs.com/52php/p/5681711.html](https://www.cnblogs.com/52php/p/5681711.html)

[Shared Libraries](https://www.cprogramming.com/tutorial/shared-libraries-linux-gcc.html)