# How gcc compiles an if check
For this code:
```
  stack *s = mkStack( 1024 );
  // ...
  push( s, 3 );
  push( s, iInteger );
  push( s, 2 );
  push( s, iInteger );

  meflCompare( s, "<" );

  if( s->memory[ s->top ] == iBoolean ) {
```

The if statement compiles to this:
```
	movq	-8(%rbp), %rax
	movl	4(%rax), %eax
	leal	-1(%rax), %edx
	movq	-8(%rbp), %rax
	movslq	%edx, %rdx
	movq	8(%rax,%rdx,8), %rax
	cmpq	$3, %rax
	jne	.L2
```

An interesting macro in the C stddef.h file: `offsetof`.  The following code:
```
  printf( "offset of size is %d\n", ( int )offsetof( stack, size ) );
  printf( "offset of top is %d\n", ( int )offsetof( stack, top ) );
  printf( "offset of memory is %d\n", ( int )offsetof( stack, memory ) );
```

Generates this output:
```
offset of size is 0
offset of top is 4
offset of memory is 8
```

Memory is an array of `long`s, which are  8 bytes each

So, the previous assembler code, annotated:
```
	movq	-8(%rbp), %rax        ; -8(%rbp) is where the stack pointer s is stored
	movl	4(%rax), %eax         ; %rax + 4 is where the 'top' field is stored.
                                  ; So, %edx is the value of the top field.
	leal	-1(%rax), %edx        ; Subtract 1 from 'top' value
	movq	-8(%rbp), %rax        ; load of stack pointer 
	movslq	%edx, %rdx            ; move sign extend long to quad
    ; displacement(base register, index register, scale factor)
    ; *(base register + displacement + (index register * scale factor))
    ; *(%rax + 8 + (%rdx * 8))
    ; %rax = pointer to stack structure
    ; %rdx = the value of the top field
    ; * 8 = because each entry in memory is 8 bytes long
    ; + 8 = because the memory field is 8 bytes past the begining of the struct
	movq	8(%rax,%rdx,8), %rax  ; move quad *(s + 8 + (top * 8)) into %rax
	cmpq	$3, %rax              ; compare iBoolean with value in %rax
	jne	.L2                       ; if they're not equal jump to label .L2
```

Now for the challenging part.  The following if statement:
```
    if( s->memory[ s->top - 1 ] != 0 ) {
```
Compiles to this:
```
	movq	-8(%rbp), %rax
	movl	4(%rax), %eax
	leal	-2(%rax), %edx
	movq	-8(%rbp), %rax
	movslq	%edx, %rdx
	movq	8(%rax,%rdx,8), %rax
	testq	%rax, %rax
	je	.L3
```

Annotated:
```
	movq	-8(%rbp), %rax         ; move the pointer to stack into %rax
	movl	4(%rax), %eax          ; move the top field into %eax (note the clobber)
	leal	-2(%rax), %edx         ; use lea instr to do quick math on contents of %rax
                                   ; store the index in %edx
	movq	-8(%rbp), %rax         ; reload the stack pointer into %rax
	movslq	%edx, %rdx             ; sign extend %edx
	movq	8(%rax,%rdx,8), %rax   ; get the data from the memory array field
	testq	%rax, %rax             ; if the value in %rax *is* zero...
	je	.L3                        ; ... jump to label .L3
```
