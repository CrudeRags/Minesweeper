package minesweeper

import kotlin.random.Random

data class CoOrdinates(val row: Int, val col: Int) {
    override fun toString(): String {
        return "($row, $col)"
    }
}

enum class Mark(val role: String, val symbol: String) {
    EMPTY("empty", "."),
    MINE("mine", "X"),
    SET_MINE("setMine", "*"),
    SET_EMPTY("setEmpty", "/");

    override fun toString(): String {
         return this.symbol
    }
}

class Minesweeper {
    private val gameField = Array(9) { Array(9) { Mark.EMPTY.symbol } }
    private val inputCoOrdinates = mutableSetOf<CoOrdinates>()
    private val mineCoOrdinates = mutableSetOf<CoOrdinates>()

    fun run() {
        gameField.addMines()
        gameField.play()
    }

    private fun Array<Array<String>>.getValue(co: CoOrdinates): String {
        return this[co.row][co.col]
    }

    private fun Array<Array<String>>.addMines() {
        var free = CoOrdinates(0,0)

        println("How many mines do you want on the field? ")
        var mines = readLine()!!.toInt()

        gameField.display()

        do {
            println("Set/unset mines marks or claim a cell as free: ")
            val (y, x, command) = readLine()!!.split(" ")
            val uC = CoOrdinates(x.toInt() - 1, y.toInt() - 1) // input coordinate
            when (command) {
                "mine" -> {
                    if(gameField[uC.row][uC.col] == Mark.SET_MINE.symbol) {
                        gameField[uC.row][uC.col] = Mark.EMPTY.symbol
                        inputCoOrdinates.remove(uC)
                    }
                    else {
                        gameField[uC.row][uC.col] = Mark.SET_MINE.symbol
                        inputCoOrdinates.add(uC)
                    }
                    gameField.display()
                }
                "free" -> free = uC
                else -> println("Invalid Command")
            }
        } while(command != "free")

        while(mines != 0) {
            val row = Random.nextInt(9)
            val col = Random.nextInt(9)
            if (row == free.row && col == free.col) continue
            else if(this[row][col] == Mark.SET_MINE.symbol) {
                mineCoOrdinates.add(CoOrdinates(row, col))
                --mines
            }
            else if (this[row][col] != Mark.MINE.symbol) {
                this[row][col] = Mark.MINE.symbol
                mineCoOrdinates.add(CoOrdinates(row, col))
                --mines
            }
        }
        this.reveal(free)
    }

    private fun Array<Array<String>>.reveal(co: CoOrdinates) {
        val row = co.row
        val col = co.col
        val mySet = mutableSetOf<CoOrdinates>()

        when (this[row][col]) {
            Mark.MINE.symbol -> return
            Mark.SET_EMPTY.symbol -> {
                if (CoOrdinates(row, col) in inputCoOrdinates) inputCoOrdinates.remove(CoOrdinates(row, col))
                return
            }
            in "123456789" -> return
            else -> {
                var check = 0
                for (x in -1..1) {
                    for (y in -1..1) {
                        try {
                            when (this[row + x][col + y]) {
                                Mark.MINE.symbol -> check++
                                Mark.SET_MINE.symbol -> {
                                    if (CoOrdinates(row + x, col + y) in mineCoOrdinates) check++
                                    mySet.add(CoOrdinates(row + x, col + y))
                                }
                                Mark.EMPTY.symbol -> mySet.add(CoOrdinates(row + x, col + y))
                            }
                        } catch(e: Exception) {}
                    }
                }
                if(check != 0) {
                    this[row][col] = check.toString()
                    mySet.clear()
                }
                else this[row][col] = Mark.SET_EMPTY.symbol
            }
        }
        for (c in mySet) {
            this.reveal(c)
        }
    }

    private fun Array<Array<String>>.mask() {
        this.forEach { row ->
            row.mapIndexed {index, s -> if(s == Mark.MINE.symbol) row[index] = Mark.EMPTY.symbol }.toTypedArray()
        }
    }

    private fun Array<Array<String>>.display() {
        println("\n" +
                " │123456789│\n" +
                "—│—————————│")
        this.indices.forEach {
            println("${it + 1}|${this[it].joinToString("")}|")
        }
        println("—│—————————│")
    }

    private fun Array<Array<String>>.countEmpty(): Int {
        var count = 0
        val myCountArray = this.map { it.copyOf() }.toTypedArray()
        myCountArray.mask()
        myCountArray.indices.forEach {
            myCountArray[it].indices.forEach { s ->
                if (myCountArray[it][s] in listOf(Mark.SET_MINE.symbol, Mark.EMPTY.symbol)) count++
            }
        }
        return count
    }

    private fun Array<Array<String>>.explode() {
        this.indices.forEach { row ->
            this[row].indices.forEach { col ->
                if (this[row][col] in listOf(Mark.SET_MINE.symbol, Mark.EMPTY.symbol)) {
                    if (CoOrdinates(row, col) in mineCoOrdinates) this[row][col] = Mark.MINE.symbol
                    else this[row][col] = Mark.EMPTY.symbol
                }
            }
        }
    }

    private fun Array<Array<String>>.play() {
        var display = true

        while(inputCoOrdinates != mineCoOrdinates && mineCoOrdinates.size != this.countEmpty()) {
            val viewField = this.map { it.copyOf() }.toTypedArray()
            viewField.mask()
            if (display) {
                viewField.display()
                display = false
            }

            println("Set/unset mines marks or claim a cell as free: ")
            val (_col, _row, doWhat) = readLine()!!.split(" ")
            val row = _row.toInt()
            val col = _col.toInt()
            val inXY = CoOrdinates(row - 1, col - 1)

            when (doWhat) {
                "free" -> {
                    if (inXY in mineCoOrdinates) {
                        this.explode()
                        this.display()
                        println("You stepped on a mine and failed!")
                        return
                    }
                    this.reveal(inXY)
                    display = true
                }
                "mine" -> {
                    when (this.getValue(inXY)) {
                        in "123456789" -> println("There is a number here!")
                        Mark.SET_EMPTY.symbol -> println("This grid has already been freed")
                        Mark.SET_MINE.symbol -> {
                            inputCoOrdinates.remove(inXY)
                            if (inXY !in mineCoOrdinates) this[inXY.row][inXY.col] = Mark.EMPTY.symbol
                            else this[inXY.row][inXY.col] = Mark.MINE.symbol
                            display = true
                        }
                        else ->  {
                            inputCoOrdinates.add(inXY)
                            this[inXY.row][inXY.col] = Mark.SET_MINE.symbol
                            display = true
                        }
                    }
                }
                else -> println("Unknown command. Command format: `3 2 free` or `3 2 mine`")
            }
        }
        this.mask()
        this.display()
        if (inputCoOrdinates == mineCoOrdinates || mineCoOrdinates.size == this.countEmpty()) println("Congratulations! You found all mines!")
    }
}