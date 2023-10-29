echo ".\result_$(Get-Date -Format "hhmmss").txt"
java -classpath .\out\production\TestComparator Main > ".\result\result_$(Get-Date -Format "hhmmss").txt"