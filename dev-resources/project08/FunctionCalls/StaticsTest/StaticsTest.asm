(Sys.init)

@6
D=A
@SP
A=M
M=D
@SP
M=M+1
@8
D=A
@SP
A=M
M=D
@SP
M=M+1
//remember current SP
@SP
D=M
@R15
M=D
@Class1.set-0
D=A
@SP
A=M
M=D
@SP
M=M+1
@LCL
D=M
@SP
A=M
M=D
@SP
M=M+1
@ARG
D=M
@SP
A=M
M=D
@SP
M=M+1
@THIS
D=M
@SP
A=M
M=D
@SP
M=M+1
@THAT
D=M
@SP
A=M
M=D
@SP
M=M+1
@R15
D=M
D=D-1
D=D-1
@ARG
// ARG -> old-SP - n-args
M=D
@SP
D=M
@LCL
M=D
@Class1.set
0;JMP
(Class1.set-0)
@SP
A=M-1
D=M
@R13
M=D
@SP
M=M-1
@5
D=A
@R14
M=D
@R13
D=M
@R14
A=M
M=D
@23
D=A
@SP
A=M
M=D
@SP
M=M+1
@15
D=A
@SP
A=M
M=D
@SP
M=M+1
//remember current SP
@SP
D=M
@R15
M=D
@Class2.set-1
D=A
@SP
A=M
M=D
@SP
M=M+1
@LCL
D=M
@SP
A=M
M=D
@SP
M=M+1
@ARG
D=M
@SP
A=M
M=D
@SP
M=M+1
@THIS
D=M
@SP
A=M
M=D
@SP
M=M+1
@THAT
D=M
@SP
A=M
M=D
@SP
M=M+1
@R15
D=M
D=D-1
D=D-1
@ARG
// ARG -> old-SP - n-args
M=D
@SP
D=M
@LCL
M=D
@Class2.set
0;JMP
(Class2.set-1)
@SP
A=M-1
D=M
@R13
M=D
@SP
M=M-1
@5
D=A
@R14
M=D
@R13
D=M
@R14
A=M
M=D
//remember current SP
@SP
D=M
@R15
M=D
@Class1.get-2
D=A
@SP
A=M
M=D
@SP
M=M+1
@LCL
D=M
@SP
A=M
M=D
@SP
M=M+1
@ARG
D=M
@SP
A=M
M=D
@SP
M=M+1
@THIS
D=M
@SP
A=M
M=D
@SP
M=M+1
@THAT
D=M
@SP
A=M
M=D
@SP
M=M+1
@R15
D=M
@ARG
// ARG -> old-SP - n-args
M=D
@SP
D=M
@LCL
M=D
@Class1.get
0;JMP
(Class1.get-2)
//remember current SP
@SP
D=M
@R15
M=D
@Class2.get-3
D=A
@SP
A=M
M=D
@SP
M=M+1
@LCL
D=M
@SP
A=M
M=D
@SP
M=M+1
@ARG
D=M
@SP
A=M
M=D
@SP
M=M+1
@THIS
D=M
@SP
A=M
M=D
@SP
M=M+1
@THAT
D=M
@SP
A=M
M=D
@SP
M=M+1
@R15
D=M
@ARG
// ARG -> old-SP - n-args
M=D
@SP
D=M
@LCL
M=D
@Class2.get
0;JMP
(Class2.get-3)
(WHILE)
@WHILE
0;JMP
(Class1.set)

@0
D=A
@ARG
A=M+D
D=M
@SP
A=M
M=D
@SP
M=M+1
@SP
A=M-1
D=M
@R13
M=D
@SP
M=M-1
@16
D=A
@R14
M=D
@R13
D=M
@R14
A=M
M=D
@1
D=A
@ARG
A=M+D
D=M
@SP
A=M
M=D
@SP
M=M+1
@SP
A=M-1
D=M
@R13
M=D
@SP
M=M-1
@17
D=A
@R14
M=D
@R13
D=M
@R14
A=M
M=D
@0
D=A
@SP
A=M
M=D
@SP
M=M+1
//start the return nightmare....
@LCL
D=M
@R13
M=D // [13] => LCL -> frame
@R13
D=M-1
D=D-1
D=D-1
D=D-1
A=D-1
D=M
@R14
M=D // [14] -> return addr
@ARG
D=M
@R15
M=D
@SP
A=M-1
D=M
@R15
A=M
M=D
@R15
D=M
D=D+1
@SP
M=D
@R13
A=M-1
D=M
@THAT
M=D // reset THAT
@R13
D=M-1
A=D-1
D=M
@THIS
M=D  // reset THIS
@R13
D=M-1
D=D-1
A=D-1
D=M
@ARG
M=D // reset ARG
@R13
D=M-1
D=D-1
D=D-1
A=D-1
D=M
@LCL
M=D // reset LCL
@R14
A=M
0;JMP // return code
(Class1.get)

@16
D=M
@SP
A=M
M=D
@SP
M=M+1
@17
D=M
@SP
A=M
M=D
@SP
M=M+1
@SP
A=M-1
D=M
@SP
A=M-1
A=A-1
M=M-D
@SP
M=M-1
//start the return nightmare....
@LCL
D=M
@R13
M=D // [13] => LCL -> frame
@R13
D=M-1
D=D-1
D=D-1
D=D-1
A=D-1
D=M
@R14
M=D // [14] -> return addr
@ARG
D=M
@R15
M=D
@SP
A=M-1
D=M
@R15
A=M
M=D
@R15
D=M
D=D+1
@SP
M=D
@R13
A=M-1
D=M
@THAT
M=D // reset THAT
@R13
D=M-1
A=D-1
D=M
@THIS
M=D  // reset THIS
@R13
D=M-1
D=D-1
A=D-1
D=M
@ARG
M=D // reset ARG
@R13
D=M-1
D=D-1
D=D-1
A=D-1
D=M
@LCL
M=D // reset LCL
@R14
A=M
0;JMP // return code
(Class2.set)

@0
D=A
@ARG
A=M+D
D=M
@SP
A=M
M=D
@SP
M=M+1
@SP
A=M-1
D=M
@R13
M=D
@SP
M=M-1
@18
D=A
@R14
M=D
@R13
D=M
@R14
A=M
M=D
@1
D=A
@ARG
A=M+D
D=M
@SP
A=M
M=D
@SP
M=M+1
@SP
A=M-1
D=M
@R13
M=D
@SP
M=M-1
@19
D=A
@R14
M=D
@R13
D=M
@R14
A=M
M=D
@0
D=A
@SP
A=M
M=D
@SP
M=M+1
//start the return nightmare....
@LCL
D=M
@R13
M=D // [13] => LCL -> frame
@R13
D=M-1
D=D-1
D=D-1
D=D-1
A=D-1
D=M
@R14
M=D // [14] -> return addr
@ARG
D=M
@R15
M=D
@SP
A=M-1
D=M
@R15
A=M
M=D
@R15
D=M
D=D+1
@SP
M=D
@R13
A=M-1
D=M
@THAT
M=D // reset THAT
@R13
D=M-1
A=D-1
D=M
@THIS
M=D  // reset THIS
@R13
D=M-1
D=D-1
A=D-1
D=M
@ARG
M=D // reset ARG
@R13
D=M-1
D=D-1
D=D-1
A=D-1
D=M
@LCL
M=D // reset LCL
@R14
A=M
0;JMP // return code
(Class2.get)

@18
D=M
@SP
A=M
M=D
@SP
M=M+1
@19
D=M
@SP
A=M
M=D
@SP
M=M+1
@SP
A=M-1
D=M
@SP
A=M-1
A=A-1
M=M-D
@SP
M=M-1
//start the return nightmare....
@LCL
D=M
@R13
M=D // [13] => LCL -> frame
@R13
D=M-1
D=D-1
D=D-1
D=D-1
A=D-1
D=M
@R14
M=D // [14] -> return addr
@ARG
D=M
@R15
M=D
@SP
A=M-1
D=M
@R15
A=M
M=D
@R15
D=M
D=D+1
@SP
M=D
@R13
A=M-1
D=M
@THAT
M=D // reset THAT
@R13
D=M-1
A=D-1
D=M
@THIS
M=D  // reset THIS
@R13
D=M-1
D=D-1
A=D-1
D=M
@ARG
M=D // reset ARG
@R13
D=M-1
D=D-1
D=D-1
A=D-1
D=M
@LCL
M=D // reset LCL
@R14
A=M
0;JMP // return code
