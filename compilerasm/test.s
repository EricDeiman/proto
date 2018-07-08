	.file	"test.c"
	.text
	.section	.rodata
.LC0:
	.string	"4 ^ 3 ^ 2 is "
	.text
	.globl	main
	.type	main, @function
main:
.LFB0:
	.cfi_startproc
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset 6, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register 6
	subq	$32, %rsp
	movl	%edi, -20(%rbp)
	movq	%rsi, -32(%rbp)
	movl	$1024, %edi
	call	mkStack@PLT
	movq	%rax, -8(%rbp)
	movq	-8(%rbp), %rax
	movl	$4, %esi
	movq	%rax, %rdi
	call	push@PLT
	movq	-8(%rbp), %rax
	movl	$1, %esi
	movq	%rax, %rdi
	call	push@PLT
	movq	-8(%rbp), %rax
	movl	$3, %esi
	movq	%rax, %rdi
	call	push@PLT
	movq	-8(%rbp), %rax
	movl	$1, %esi
	movq	%rax, %rdi
	call	push@PLT
	movq	-8(%rbp), %rax
	movl	$2, %esi
	movq	%rax, %rdi
	call	push@PLT
	movq	-8(%rbp), %rax
	movl	$1, %esi
	movq	%rax, %rdi
	call	push@PLT
	movq	-8(%rbp), %rax
	movq	%rax, %rdi
	call	meflPow@PLT
	movq	-8(%rbp), %rax
	movq	%rax, %rdi
	call	meflPow@PLT
	leaq	.LC0(%rip), %rdi
	movl	$0, %eax
	call	printf@PLT
	movq	-8(%rbp), %rax
	movq	%rax, %rdi
	call	printTos@PLT
	movl	$10, %edi
	call	putchar@PLT
	movl	$0, %eax
	leave
	.cfi_def_cfa 7, 8
	ret
	.cfi_endproc
.LFE0:
	.size	main, .-main
	.ident	"GCC: (Ubuntu 7.3.0-16ubuntu3) 7.3.0"
	.section	.note.GNU-stack,"",@progbits
