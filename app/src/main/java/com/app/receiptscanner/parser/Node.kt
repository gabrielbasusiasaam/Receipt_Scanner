package com.app.receiptscanner.parser

data class Node(
    val content: ArrayList<String>,
    val parentNode: Node?,
    val childNodes: ArrayList<Node>,
    val isLeaf: Boolean,
    val regex: List<String>? = null,
) {
    override fun toString() =
        "[content=$content, parentNode=${parentNode?.content}, childNodes=$childNodes, isLeaf=$isLeaf]"
}

