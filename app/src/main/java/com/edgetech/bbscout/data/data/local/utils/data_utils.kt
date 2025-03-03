package com.edgetech.bbscout.data.data.local.utils



class StringList: ArrayList<String>(){
    companion object{
        fun fromList(list: List<String>?): StringList?{
            if (list.isNullOrEmpty()){
                return null
            }
            val stringList = StringList()
            stringList.addAll(list)
            return stringList
        }
    }
}

class LongList: ArrayList<Long>(){
    companion object {
        fun fromList(list: List<Long>?): LongList? {
            if (list.isNullOrEmpty()) {
                return null
            }
            val longList = LongList()
            longList.addAll(list)
            return longList
        }

    }
}