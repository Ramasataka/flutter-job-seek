package com.example.uasjobportal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.uasjobportal.Models.JobData
import com.example.uasjobportal.utils.CurrencyHelper

class JobSeekerAdapter(
    private var jobList: List<JobData>,
    private val onApplyClick: (Int) -> Unit
) : RecyclerView.Adapter<JobSeekerAdapter.JobViewHolder>() {

    class JobViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.textViewJobTitle)
        val tvCompany: TextView = itemView.findViewById(R.id.textViewCompanyName)
        val tvLocation: TextView = itemView.findViewById(R.id.textViewLocation)
        val tvSalary: TextView = itemView.findViewById(R.id.textViewSalary)
        val tvDesc: TextView = itemView.findViewById(R.id.textViewJobDesc)
        val btnApply: Button = itemView.findViewById(R.id.buttonApply)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.listcard_seeker, parent, false)
        return JobViewHolder(view)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        val job = jobList[position]

        holder.tvTitle.text = job.title
        holder.tvCompany.text = job.company.companyName // Ambil dari nested object
        holder.tvLocation.text = job.location
        holder.tvSalary.text = CurrencyHelper.formatRupiah(job.salary)
        holder.tvDesc.text = job.jobDesc

        holder.btnApply.setOnClickListener {
            // Panggil callback yang dikirim dari MainActivity
            // Kirim ID job yang dipilih
            onApplyClick(job.id)
        }
    }

    override fun getItemCount(): Int {
        return jobList.size
    }

    // Fungsi helper untuk update data dari Activity
    fun setData(newList: List<JobData>) {
        jobList = newList
        notifyDataSetChanged()
    }
}