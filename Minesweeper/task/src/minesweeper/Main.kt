package minesweeper

import kotlin.random.Random

const val empty = "."
const val mine = "X"
const val setMine = "*"
const val setEmpty = "/"


data class CoOrdinates(val row: Int, val col: Int) {
    override fun toString(): String {
        return "($row, $col)"
    }
}

fun main() {
    val (gameField, firstC) = createMineField()
    val minePositions = getCoordinates(gameField)

    gameField.reveal(firstC)

    val viewField = gameField.map { it.copyOf() }.toTypedArray()
    viewField.mask()
    viewField.display()

    gameField.play(minePositions)
}

fun Array<Array<String>>.getValue(co: CoOrdinates): String {
    return this[co.row][co.col]
}

fun createMineField(): Pair<Array<Array<String>>, CoOrdinates> {
    val mineField = Array(9) { Array(9) { empty } }

    println("How many mines do you want on the field? ")
    var mines = readLine()!!.toInt()

    mineField.display()

    println("Set/unset mines marks or claim a cell as free: ")
    val (y, x, _) = readLine()!!.split(" ")

    while(mines != 0) {
        val row = Random.nextInt(9)
        val col = Random.nextInt(9)
        if (row == x.toInt() - 1 && col == y.toInt() - 1) continue
        else if (mineField[row][col] != mine) {
            mineField[row][col] = mine
            --mines
        }
    }

    return mineField to CoOrdinates(x.toInt() - 1, y.toInt() - 1)
}

fun Array<Array<String>>.reveal(co: CoOrdinates) {
    val row = co.row
    val col = co.col
    val mySet = mutableSetOf<CoOrdinates>()

    when (this[row][col]) {
        mine, setEmpty -> return
        in "123456789" -> return
        else -> {
            var check = 0
            for (x in -1..1) {
                for (y in -1..1) {
                    try {
                        when (this[row + x][col + y]) {
                            mine -> check++
                            empty -> mySet.add(CoOrdinates(row + x, col + y))
                        }
                    } catch(e: Exception) {}
                }
            }
            if(check != 0) {
                this[row][col] = check.toString()
                mySet.clear()
            }
            else this[row][col] = setEmpty
        }
    }
    for (c in mySet) {
        this.reveal(c)
    }
}

fun Array<Array<String>>.mask() {
    this.forEach { row ->
        row.mapIndexed {index, s -> if(s == mine) row[index] = empty }.toTypedArray()
    }
}

fun Array<Array<String>>.display() {
    println("\n" +
            " │123456789│\n" +
            "—│—————————│")
    this.indices.forEach {
        println("${it + 1}|${this[it].joinToString("")}|")
    }
    println("—│—————————│")
}

fun getCoordinates(mineField: Array<Array<String>>): Set<CoOrdinates> {
    val myCoOrdinates: MutableSet<CoOrdinates> = mutableSetOf()
    for(row in mineField.indices) {
        for(col in mineField[row].indices) {
            if(mineField[row][col] == mine) myCoOrdinates.add(CoOrdinates(row, col))
        }
    }
    return myCoOrdinates
}

fun Array<Array<String>>.play(mineCoOrdinates: Set<CoOrdinates>) {
    val inputCoOrdinates = mutableSetOf<CoOrdinates>()
    var display = false

    while(inputCoOrdinates != mineCoOrdinates) {
        var viewField = this.map { it.copyOf() }.toTypedArray()
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

        if (doWhat == "free") {
            if (this.getValue(inXY) == mine) {
                this.display()
                println("You stepped on a mine and failed!")
                return
            }
            this.reveal(inXY)
            display = true
        }
        else if (doWhat == "mine") {
            when (this.getValue(inXY)) {
                empty -> {
                    inputCoOrdinates.add(inXY)
                    this[inXY.row][inXY.col] = setMine
                    display = true
                }
                in (1..9).map { it.toString() } -> println("There is a number here!")
                else -> {
                    val removeMine = CoOrdinates(row, col)
                    inputCoOrdinates.remove(removeMine)
                    this[removeMine.row][removeMine.col] = empty
                    display = true
                }
            }
        }
        else println("Unknown command. Command format: `3 2 free` or `3 2 mine`")
    }
    this.display()
    if (inputCoOrdinates == mineCoOrdinates) println("Congratulations! You found all mines!")
}
