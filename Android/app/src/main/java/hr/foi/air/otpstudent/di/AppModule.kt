package hr.foi.air.otpstudent.di

import com.google.firebase.firestore.FirebaseFirestore
import hr.foi.air.otpstudent.data.repository.FirebaseJobRepositoryImpl
import hr.foi.air.otpstudent.data.source.remote.JobsRemoteDataSource
import hr.foi.air.otpstudent.data.repository.FirebaseCvRepositoryImpl
import hr.foi.air.otpstudent.domain.repository.CvRepository
import com.google.firebase.auth.FirebaseAuth
import hr.foi.air.otpstudent.data.auth.FirebaseAuthRepositoryImpl
import hr.foi.air.otpstudent.domain.repository.AuthRepository
import hr.foi.air.otpstudent.data.repository.FirebaseInternshipRepositoryImpl
import hr.foi.air.otpstudent.data.source.remote.InternshipsRemoteDataSource


object AppModule {


    private val firebaseAuth by lazy { FirebaseAuth.getInstance() }

    // repositories
    val authRepository: AuthRepository by lazy {
        FirebaseAuthRepositoryImpl(firebaseAuth, firestore)
    }
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    // data sources
    private val jobsRemote by lazy { JobsRemoteDataSource(firestore) }

    // repositories
    val jobRepository by lazy { FirebaseJobRepositoryImpl(jobsRemote) }

    fun provideCvRepository(userId: String): CvRepository = FirebaseCvRepositoryImpl(userId)

    private val internshipsRemote by lazy { InternshipsRemoteDataSource(firestore) }
    val internshipRepository by lazy { FirebaseInternshipRepositoryImpl(internshipsRemote) }



}
