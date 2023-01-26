package com.app.receiptscanner.parser

data class Node(val content: String, val parentNode: Node?, val childNodes: ArrayList<Node>,val isLeaf: Boolean)

