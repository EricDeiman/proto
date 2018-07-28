for f in tests/*.minefield
do
    java -jar jars/interpreter.jar $f > $f.out.i
    java -jar jars/compilervm.jar $f
    java -jar jars/virtualmachine.jar ${f/.minefield/.mo} > $f.out.vm
    java -jar jars/compilerasm.jar $f
    gcc ${f/.minefield/.s} compilerasm/minefieldrt.o -lm -g -o ${f/.minefield/}
    ./${f/.minefield/} > $f.out.asm
    cmp -s $f.out.i $f.out.vm
    if [ $? -ne 0 ]; then
        echo Trouble between interpreter and vm  with $f
    fi
    cmp -s $f.out.i $f.out.asm
    if [ $? -ne 0 ]; then
        echo Trouble between interpreter and asm with $f
    fi
done
