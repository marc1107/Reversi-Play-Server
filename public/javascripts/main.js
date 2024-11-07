let selectedSound = 'click-sound-click1';
// let player1Name  = 'Spieler1';
// let player2Name  = 'Spieler2';
//
//
//
// document.getElementById("play-button").addEventListener("click", function(event) {
//     event.preventDefault(); // Prevent the default action (navigation)
//     savePlayerNames(); // Call the function to save player names
//     window.location.href = this.href; // Navigate to the game page
// });
//
// function savePlayerNames() {
//     // Your logic to save player names
//     const player1Name = document.getElementById("player1").value;
//     const player2Name = document.getElementById("player2").value;
//     console.log("Player 1:", player1Name);
//     console.log("Player 2:", player2Name);
//     // Add your saving logic here
// }

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
// Update the current player display
    const playerClass = currentPlayer === "B" ? "black" : "white";
    $("#playerturn1").attr("class", `playerturn ${playerClass}`);
    $("#playerturn2").attr("class", `playerturn ${playerClass}`);
    $("#playerturn").text(currentPlayer === "B" ? "Player 1" : "Player 2");
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

// DragDrop

document.addEventListener("DOMContentLoaded", function () {
    // Initialisierung der Variablen
    const playerInfo = document.querySelector(".playerinfo");
    const dragPiece = document.getElementById("drag-piece");
    let isDragging = false; // Status, ob das Dragging aktiv ist oder nicht

    // Funktion zum Anzeigen des Zylinders und Starten des Dragging
    playerInfo.addEventListener("mousedown", function (event) {
        event.preventDefault(); // Verhindert das Markieren von Inhalten
        isDragging = true; // Aktiviert das Dragging

        // Setze die Farbe des Zylinders basierend auf playerState
        if (playerInfo.querySelector(".black")) {
            dragPiece.style.backgroundColor = "black";
        } else {
            dragPiece.style.backgroundColor = "white";
        }

        // Zylinder anzeigen und auf den Mauszeiger positionieren
        dragPiece.style.display = "block";
        dragPiece.style.position = "fixed";
        dragPiece.style.left = `${event.clientX}px`;
        dragPiece.style.top = `${event.clientY}px`;

        // Starte das Verfolgen der Mausbewegungen
        document.addEventListener("mousemove", movePiece);
    });

    // Mausbewegungsfunktion, um den Zylinder der Maus folgen zu lassen
    function movePiece(event) {
        if (isDragging) {
            dragPiece.style.left = `${event.clientX}px`;
            dragPiece.style.top = `${event.clientY}px`;
        }
    }

    // Versteckt den Zylinder und beendet das Dragging, wenn die Maustaste losgelassen wird
    document.addEventListener("mouseup", function () {
        if (isDragging) {
            dragPiece.style.display = "none";
            document.removeEventListener("mousemove", movePiece);
            isDragging = false; // Deaktiviert das Dragging
        }
    });

    // Hole alle <td>-Elemente und füge den mouseup-Event-Listener hinzu
    const cells = document.querySelectorAll("td[data-row][data-cell]");
    cells.forEach(cell => {
        cell.addEventListener("mouseup", function () {
            // Wenn gerade gezogen wird (Zylinder sichtbar), rufe makeMove auf
            if (isDragging) {
                const row = cell.getAttribute("data-row");
                const col = cell.getAttribute("data-cell");
                makeMove(row, col);
            }
        });
    });
});

// Die makeMove-Funktion
// function makeMove(row, col) {
//     console.log("Move made at row:", row, "col:", col);
//     // Hier kannst du die Logik für einen Zug hinzufügen
// }
