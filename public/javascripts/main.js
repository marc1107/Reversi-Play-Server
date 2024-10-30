let selectedSound = 'click-sound-click1';

function makeMove(row, col) {
    console.log("Move made at row: " + row + ", column: " + col);
    $.ajax({
        url: `/makeMoveAjax/${row}/${col}`,
        method: 'GET',
        success: function(response) {
            updateBoard(response);
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

    let changed = false;

    // Update the board cells
    for (let row = 0; row < size; row++) {
        for (let col = 0; col < size; col++) {
            const cell = $(`td[data-row='${row + 1}'][data-cell='${col + 1}']`);
            const oldStone = oldBoard[row][col];
            const newStone = updatedBoard[row][col];
            let content;

            if (oldStone === "B" && newStone === "W") {
                changed = true;
                content = "<div class='stone black flip-to-white'></div>";
            } else if (oldStone === "W" && newStone === "B") {
                changed = true;
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

    if (!changed) {
        playErrorSound();
        return;
    }

    playClickSound();

    // Update the current player display
    const playerClass = currentPlayer === "B" ? "black" : "white";
    $("#playerturn1").attr("class", `playerturn ${playerClass}`);
    $("#playerturn2").attr("class", `playerturn ${playerClass}`);
}

function changeClickSound() {
    const selectedValue = document.getElementById('sound-select').value;
    selectedSound = 'click-sound-' + selectedValue;
}

function playClickSound() {
    const clickSound = document.getElementById(selectedSound);
    clickSound.play();
}

function playErrorSound() {
    const errorSound = document.getElementById('error-sound1');
    errorSound.play();
}

function playButtonClickSound() {
    const clickSound = document.getElementById('button-click-sound');
    clickSound.play();
}