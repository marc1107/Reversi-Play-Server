function makeMove(row, col) {
    console.log("Move made at row: " + row + ", column: " + col);
    $.ajax({
        url: `/makeMoveAjax/${row}/${col}`,
        method: 'GET',
        success: function(response) {
            updateBoard(response);
            playClickSound();
        },
        error: function(xhr, status, error) {
            console.error("Error making move:", error);
        }
    });
}

function updateBoard(response) {
    const oldBoard = response.oldBoard.cells;
    const updatedBoard = response.newBoard.cells;
    const size = response.newBoard.size;
    const currentPlayer = response.newBoard.playerState;

    // Update the board cells
    for (let row = 0; row < size; row++) {
        for (let col = 0; col < size; col++) {
            const cell = $(`td[data-row='${row + 1}'][data-cell='${col + 1}']`);
            const oldStone = oldBoard[row][col];
            const newStone = updatedBoard[row][col];
            let content;

            if (oldStone === "B" && newStone === "W") {
                content = "<div class='stone black flip-to-white'></div>";
            } else if (oldStone === "W" && newStone === "B") {
                content = "<div class='stone white flip-to-black'></div>";
            } else if (newStone === "B") {
                content = "<div class='stone black'></div>";
            } else if (newStone === "W") {
                content = "<div class='stone white'></div>";
            } else {
                content = "&nbsp;";
            }

            cell.html(content);
        }
    }

    // Update the current player display
    const playerClass = currentPlayer === "B" ? "black" : "white";
    $("#playerturn1").attr("class", `playerturn ${playerClass}`);
    $("#playerturn2").attr("class", `playerturn ${playerClass}`);
}

function playClickSound() {
    const clickSound = document.getElementById('click-sound');
    clickSound.play();
}