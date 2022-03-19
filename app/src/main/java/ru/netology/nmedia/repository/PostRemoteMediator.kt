package ru.netology.nmedia.repository

import androidx.paging.*
import androidx.room.withTransaction
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dao.PostRemoteKeyDao
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.PostRemoteKeyEntity
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.ApiError
import java.lang.Exception
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
class PostRemoteMediator @Inject constructor(
    private val service: ApiService,
    private val postDao: PostDao,
    private val remoteKeyDao: PostRemoteKeyDao,
    private val db: AppDb
) : RemoteMediator<Int, PostEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostEntity>
    ): MediatorResult {
        try {
            val response = when (loadType) {
                LoadType.REFRESH -> {
                    service.getLatest(state.config.initialLoadSize)
                }
                LoadType.PREPEND -> {
                    return MediatorResult.Success(false)
                }
                LoadType.APPEND -> {
                    val id = remoteKeyDao.min() ?: return MediatorResult.Success(
                        endOfPaginationReached = false
                    )
                    service.getBefore(id, state.config.pageSize)
                }
            }

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(
                response.code(),
                response.message()
            )

            db.withTransaction {
                when (loadType) {
                    LoadType.REFRESH -> {
                        remoteKeyDao.insert(
                            PostRemoteKeyEntity(
                                type = PostRemoteKeyEntity.KeyType.AFTER,
                                id = body.first().id
                            )
                        )
                    }
                    LoadType.APPEND -> {
                        remoteKeyDao.insert(
                            PostRemoteKeyEntity(
                                type = PostRemoteKeyEntity.KeyType.BEFORE,
                                id = body.last().id
                            )
                        )
                    }
                }
                postDao.insert(body.toEntity())
            }
            return MediatorResult.Success(endOfPaginationReached = body.isEmpty())
        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }
    }
}