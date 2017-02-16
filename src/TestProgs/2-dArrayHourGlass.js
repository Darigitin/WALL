process.stdin.resume();
process.stdin.setEncoding('ascii');

var input_stdin = "";
var input_stdin_array = "";
var input_currentline = 0;

process.stdin.on('data', function (data) {
    input_stdin += data;
});

process.stdin.on('end', function () {
    input_stdin_array = input_stdin.split("\n");
    main();    
});

function readLine() {
    return input_stdin_array[input_currentline++];
}

/////////////// ignore above this line ////////////////////

function main() {
    var arr = [];
    for(arr_i = 0; arr_i < 6; arr_i++){
       arr[arr_i] = readLine().split(' ');
       arr[arr_i] = arr[arr_i].map(Number);
    }
    
    let maxSum, zeroFlag = false;
    for (let i = 0; i < 4; i++) { //row
        for (let j = 0; j < 4; j++) { //column
            let current = arr[i][j] + arr[i][j+1] + arr[i][j+2] + arr[i+1][j+1] + arr[i+2][j] + arr[i+2][j+1] + arr[i+2][j+2];
            if (!maxSum)
                maxSum = current;
            else if (current === 0)
                zeroFlag = true;
            else if (current < 0 && maxSum < 0 && Math.abs(current) < Math.abs(maxSum))
                maxSum = current;
            else if (current > 0 && current > maxSum)
                maxSum = current;
        }
    }
    if (maxSum < 0 && zeroFlag)
        console.log(0);
    else
        console.log(maxSum);

}
