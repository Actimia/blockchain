
class BlockchainMain {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val genesis = GenesisBlock()
            val transactions = listOf(
                Transaction(1, 2, 10),
                Transaction(2, 3, 5)
            )
            val block = Block(genesis, transactions, 2)

            println(block.hash)
            println(block.getBalances())

            val transactions2 = listOf(
                Transaction(2, 4, 12),
                Transaction(3, 1, 2)
            )
            val block2 = Block(block, transactions2, 1)
            println()
            println(block2.hash)
            println(block2.getBalances())
        }
    }
}

interface IBlock {
    val hash : Int
    val transactions : List<Transaction>
    val previous : IBlock?

    fun getBalances() : Map<Int, Int>
}

class GenesisBlock : IBlock {
    override val hash = 0
    override val transactions: List<Transaction> = listOf(Transaction(0, 1, 100))
    override val previous: IBlock? = null
    override fun getBalances(): Map<Int, Int> {
        return mapOf(Pair(1, 100))
    }
}

class Block (override val previous : IBlock, transactions : List<Transaction>, miner : Int) : IBlock {
    override val hash : Int
    override val transactions : List<Transaction>
    val nonce : Int

    init {
        val withReward = transactions.toMutableList()
        withReward.add(Transaction(0, miner, difficulty))
        this.transactions = withReward.toList()

        var nonce = -1
        var hash : Int
        do {
            hash = hash(previous, transactions, ++nonce)

        } while (!trailingZeros(hash, difficulty))
        this.nonce = nonce
        this.hash = hash
    }

    override fun getBalances(): Map<Int, Int> {
        val state = previous.getBalances().toMutableMap()
        for (trans in transactions) {
            state.computeIfPresent(trans.sender, {_, amount ->  amount - trans.amount})
            state.computeIfPresent(trans.receiver, {_, amount -> amount + trans.amount})
            state.computeIfAbsent(trans.receiver, { trans.amount })

        }
        return state.toMap()
    }

    companion object {
        val difficulty = 4

        fun hash(prev: IBlock, transactions: List<Transaction>, nonce: Int) : Int {
            return prev.hash + transactions.map { it.hash() }.sum() + nonce
        }
    }

    private fun trailingZeros(candidate : Int, limit: Int) : Boolean {
        return (0..limit).all {
            (candidate ushr it) and 1 == 0
        }
    }

}


class Transaction (val sender: Int, val receiver : Int, val amount : Int) {
    fun hash() : Int {
        return sender + receiver + amount
    }
}