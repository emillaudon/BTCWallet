package com.example.wallet

import androidx.room.*

@Dao
interface KeyHolderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(keyHolder: KeyHolder)

    @Delete
    fun delete(keyHolder: KeyHolder)

    @Update
    fun update(keyHolder: KeyHolder)

    @Query("SELECT * FROM `keyholder` WHERE id =:id")
    fun loadKeyHolder(id: Int): KeyHolder?

}
