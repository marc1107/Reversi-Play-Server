let selectedSound = 'click-sound-click1';
let currentField;
let currentPlayer;
let hintsLevel = 1;
var websocket;

document.addEventListener("DOMContentLoaded", function() {
    const player1Input = document.getElementById("player1");
    const player2Input = document.getElementById("player2");
    const welcomeMessage = document.getElementById("welcome-message");
    const playButton = document.getElementById("play-button");

    // Aktualisiere die Begrüßungsnachricht in Echtzeit
    function updateWelcomeMessage() {
        const player1Name = player1Input.value || "Spieler1";
        const player2Name = player2Input.value || "Spieler2";
        welcomeMessage.textContent = `Welcome ${player1Name} und ${player2Name}!`;
    }

    // Event-Listener für die Eingabefelder
    player1Input.addEventListener("input", updateWelcomeMessage);
    player2Input.addEventListener("input", updateWelcomeMessage);

    // Speichere die Namen in localStorage beim Klicken auf den Button
    playButton.addEventListener("click", function(event) {
        event.preventDefault(); // Verhindert das Standardverhalten des Links
        localStorage.setItem("player1Name", player1Input.value);
        localStorage.setItem("player2Name", player2Input.value);
        window.location.href = playButton.href; // Navigiert zur nächsten Seite
    });
});

function updatePlayerNames(currentPlayerState) {
    // Spielername aus localStorage holen oder Fallback-Werte verwenden
    const player1Name = localStorage.getItem("player1Name") || "Spieler1";
    const player2Name = localStorage.getItem("player2Name") || "Spieler2";

    // Element für die aktuelle Spieleranzeige finden
    const playerDisplay = document.getElementById("playerturn");

    // Aktualisiere den angezeigten Namen basierend auf dem aktuellen Spielerstatus
    if (currentPlayerState === "B") {
        playerDisplay.textContent = player1Name;
    } else if (currentPlayerState === "W") {
        playerDisplay.textContent = player2Name;
    }
}

function showAllowedHints() {
    if (hintsLevel !== 2)
        return;

    for (let row = 0; row < 8; row++) {
        for (let col = 0; col < 8; col++) {
            showHint(row, col);
        }
    }
}

function showHintFor(row, col) {
    if (hintsLevel === 1)
        showHint(row, col);
}

function showHint(row, col) {
    const hint = $(`td[data-row='${row + 1}'][data-cell='${col + 1}']`);

    if (hint.find('.stone').length !== 0) {
        return;
    }

    if (hintsLevel === 1)
        hideAllHints();

    const possible = isMovePossible(row, col);
    // 1 = move is possible
    // 0 = move is not possible
    // -1 = cell is not empty
    if (possible === 1) {
        if (hint.html().trim() === "&nbsp;") {
            hint.html('');  // Clear the cell content
        }

        if (hint.find('.stone').length === 0) {
            const stoneDiv = currentPlayer === "B"
                ? $('<div class="stone black hint"></div>')
                : $('<div class="stone white hint"></div>');

            // Append stone div to td
            hint.html(stoneDiv);
        }
    } else if (possible === 0) {
        hint.html('<div></div>');
    }
}

function hideAllHints() {
    for (let row = 0; row < 8; row++) {
        for (let col = 0; col < 8; col++) {
            if (currentField[row][col] !== "EMPTY") {
                continue;
            }
            const hint = $(`td[data-row='${row + 1}'][data-cell='${col + 1}']`);
            hint.html('<div></div>');
        }
    }
}

function isMovePossible(row, col) {
    if (currentField[row][col] !== "EMPTY") {
        return -1;
    }

    const directions = [
        [-1, 0], [1, 0], // vertical
        [0, -1], [0, 1], // horizontal
        [-1, -1], [1, 1], // diagonal
        [-1, 1], [1, -1]  // anti-diagonal
    ];

    const opponent = currentPlayer === "B" ? "W" : "B";

    for (let [dx, dy] of directions) {
        let x = row + dx;
        let y = col + dy;
        let foundOpponent = false;

        while (x >= 0 && x < 8 && y >= 0 && y < 8) {
            if (currentField[x][y] === opponent) {
                foundOpponent = true;
            } else if (currentField[x][y] === currentPlayer) {
                if (foundOpponent) {
                    return 1;
                } else {
                    break;
                }
            } else {
                break;
            }
            x += dx;
            y += dy;
        }
    }

    return 0;
}

function makeMove(row, col) {
    websocket.send(JSON.stringify({row: row, col: col}));

    /*$.ajax({
        url: `/makeMoveAjax/${row}/${col}`,
        method: 'GET',
        success: function(response) {
            updateBoard(response.newBoard);
        },
        error: function(xhr, status, error) {
            console.error("Error making move:", error);
        }
    });*/
}

function updateBoard(newBoard) {
    const updatedBoard = newBoard.cells;
    const size = newBoard.size;
    currentPlayer = newBoard.playerState;

    let changed = false;

    // Update the board cells
    for (let row = 0; row < size; row++) {
        for (let col = 0; col < size; col++) {
            const cell = $(`td[data-row='${row + 1}'][data-cell='${col + 1}']`);
            const oldStone = currentField[row][col];
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
                content = "<div></div>";
            }

            cell.html(content);
        }
    }

    currentField = updatedBoard;

    if (!changed) {
        playErrorSound();
        showAllowedHints();
        return;
    }

    playClickSound();

    // Update the current player display
    const playerClass = currentPlayer === "B" ? "black" : "white";
    $("#playerturn1").attr("class", `playerturn ${playerClass}`);
    $("#playerturn2").attr("class", `playerturn ${playerClass}`);
    $("#playerturn").text(currentPlayer === "B" ? "Player 1" : "Player 2");
    updatePlayerNames(currentPlayer);
    showAllowedHints();
}

function changeHintsLevel() {
    const selectedValue = document.getElementById('hint-select').value;
    hintsLevel = parseInt(selectedValue);
    hideAllHints();
    showAllowedHints();
}

function connectWebSocket() {
    websocket = new WebSocket("ws://localhost:9000/websocket");
    websocket.setTimeout

    websocket.onopen = function(event) {
        console.log("Connected to Websocket");
    }

    websocket.onclose = function () {
        console.log('Connection with Websocket Closed!');
    };

    websocket.onerror = function (error) {
        console.log('Error in Websocket Occured: ' + error);
    };

    websocket.onmessage = function (e) {
        if (typeof e.data === "string") {
            let newBoard = JSON.parse(e.data);
            updateBoard(newBoard);
        }

    };
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
    const playerInfo = document.querySelector(".playerinfo");
    const dragPiece = document.getElementById("drag-piece");
    let isDragging = false; // Status, ob das Dragging aktiv ist oder nicht

    playerInfo.addEventListener("mousedown", function (event) {
        event.preventDefault();
        isDragging = true;

        // Setze die Farbe des Zylinders basierend auf playerState
        if (playerInfo.querySelector(".black")) {
            dragPiece.style.backgroundColor = "black";
        } else {
            dragPiece.style.backgroundColor = "white";
        }

        // Zylinder anzeigen und auf den Mauszeiger positionieren
        dragPiece.style.display = "block";
        dragPiece.style.position = "fixed";
        dragPiece.style.left = `${event.clientX-30}px`;
        dragPiece.style.top = `${event.clientY-30}px`;

        // Starte das Verfolgen der Mausbewegungen
        document.addEventListener("mousemove", movePiece);
    });

    // Mausbewegungsfunktion, um den Zylinder der Maus folgen zu lassen
    function movePiece(event) {
        if (isDragging) {
            dragPiece.style.left = `${event.clientX-30}px`;
            dragPiece.style.top = `${event.clientY-30}px`;
        }
    }

    // Versteckt den Zylinder und beendet das Dragging, wenn die Maustaste losgelassen wird
    document.addEventListener("mouseup", function () {
        if (isDragging) {
            dragPiece.style.display = "none";
            document.removeEventListener("mousemove", movePiece);
            isDragging = false;
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

function getField() {
    $.ajax({
        url: '/getField',
        method: 'GET',
        success: function(response) {
            currentField = response.newBoard.cells;
            updateBoard(response.newBoard);
        },
        error: function(xhr, status, error) {
            console.error("Error getting field:", error);
        }
    });
}

$( document ).ready(function() {
    getField();
    connectWebSocket()
});
