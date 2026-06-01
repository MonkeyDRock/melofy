package com.example.melofy.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.melofy.domain.model.PodcastEpisode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class PodcastViewModel @Inject constructor() : ViewModel() {

    private val _episodes = MutableStateFlow<List<PodcastEpisode>>(emptyList())
    val episodes: StateFlow<List<PodcastEpisode>> = _episodes.asStateFlow()

    private val _activeEpisode = MutableStateFlow<PodcastEpisode?>(null)
    val activeEpisode: StateFlow<PodcastEpisode?> = _activeEpisode.asStateFlow()

    init {
        loadPodcastEpisodes()
    }

    private fun loadPodcastEpisodes() {
        _episodes.value = listOf(
            // ─── TECH & SCIENCE ──────────────────────────────────────
            PodcastEpisode(
                id = "ycPr5-27vSI",
                title = "Elon Musk #1169 (2018)",
                description = "Joe Rogan sits down with Elon Musk to talk about AI, Tesla, SpaceX, Neuralink, and the future of humanity in one of the most viral podcast episodes of all time.",
                host = "The Joe Rogan Experience",
                category = "Tech & Science",
                thumbnailUrl = "https://img.youtube.com/vi/ycPr5-27vSI/maxresdefault.jpg"
            ),
            PodcastEpisode(
                id = "jSqCL7Npln0",
                title = "Andrew Huberman – Control Your Dopamine",
                description = "Neuroscientist Dr. Andrew Huberman explains the neurobiology of dopamine, how to regulate motivation, avoid burnout, and master your drive.",
                host = "The Diary of a CEO",
                category = "Tech & Science",
                thumbnailUrl = "https://img.youtube.com/vi/jSqCL7Npln0/maxresdefault.jpg"
            ),
            PodcastEpisode(
                id = "VYypt0QB7FM",
                title = "Stop Doing This To Yourself",
                description = "Mel Robbins breaks down research-backed steps to stop self-sabotage, conquer procrastination, and rebuild confidence in your daily habits.",
                host = "The Mel Robbins Podcast",
                category = "Tech & Science",
                thumbnailUrl = "https://img.youtube.com/vi/VYypt0QB7FM/maxresdefault.jpg"
            ),
            PodcastEpisode(
                id = "9SXKSGz24sI",
                title = "How We Paid Off $127K in Debt",
                description = "Dave Ramsey and team celebrate a couple's debt-free scream and break down the financial strategies and habits used to eliminate debt fast.",
                host = "The Ramsey Show",
                category = "Tech & Science",
                thumbnailUrl = "https://img.youtube.com/vi/9SXKSGz24sI/maxresdefault.jpg"
            ),
            PodcastEpisode(
                id = "RVbqMg7IYPU",
                title = "#1 Ethical Hacker Catches Predator Live",
                description = "Shawn Ryan interviews an elite cybersecurity expert and ethical hacker about active predator traps, digital defense, and modern network safety.",
                host = "The Shawn Ryan Show",
                category = "Tech & Science",
                thumbnailUrl = "https://img.youtube.com/vi/RVbqMg7IYPU/maxresdefault.jpg"
            ),
            PodcastEpisode(
                id = "2uYs0gJD-LE",
                title = "Sadhguru Full Interview",
                description = "Ranveer Allahbadia hosts Sadhguru for a deep exploration of consciousness, human potential, spirituality, and ancient Indian wisdom.",
                host = "The BeerBiceps Podcast",
                category = "Tech & Science",
                thumbnailUrl = "https://img.youtube.com/vi/2uYs0gJD-LE/maxresdefault.jpg"
            ),

            // ─── MUSIC & CREATORS ────────────────────────────────────
            PodcastEpisode(
                id = "jgFkFDnCmVQ",
                title = "World's Hottest Pepper – Carolina Reaper",
                description = "Rhett and Link face off in the ultimate spicy food challenge: eating the legendary Carolina Reaper, testing their tastebuds to the limit.",
                host = "Good Mythical Morning",
                category = "Music & Creators",
                thumbnailUrl = "https://img.youtube.com/vi/jgFkFDnCmVQ/maxresdefault.jpg"
            ),
            PodcastEpisode(
                id = "6BHkSMGZIus",
                title = "Super Bowl Week Special",
                description = "Travis and Jason Kelce break down Super Bowl preparations, media week chaos, and share brotherly banter ahead of the big game.",
                host = "New Heights",
                category = "Music & Creators",
                thumbnailUrl = "https://img.youtube.com/vi/6BHkSMGZIus/maxresdefault.jpg"
            ),
            PodcastEpisode(
                id = "ZGixaRJHFLA",
                title = "KSI vs Logan Paul Fight Preview",
                description = "Logan Paul and his crew sit down with KSI to talk about their boxing rivalry, YouTube creators, and building their global brand together.",
                host = "Impaulsive",
                category = "Music & Creators",
                thumbnailUrl = "https://img.youtube.com/vi/ZGixaRJHFLA/maxresdefault.jpg"
            ),
            PodcastEpisode(
                id = "o_jlExECMlc",
                title = "Shannon Sharpe & Chad Ochocinco",
                description = "Shannon Sharpe and legendary receiver Chad Ochocinco talk about NFL highlights, career stories, fitness, and modern sports culture.",
                host = "Club Shay Shay",
                category = "Music & Creators",
                thumbnailUrl = "https://img.youtube.com/vi/o_jlExECMlc/maxresdefault.jpg"
            ),
            PodcastEpisode(
                id = "D7fKIbbwWLo",
                title = "Trisha Paytas Episode",
                description = "Trisha Paytas joins the Frenemies set with Ethan Klein for a hilarious and drama-filled conversation about YouTube culture and their personal updates.",
                host = "H3 Podcast",
                category = "Music & Creators",
                thumbnailUrl = "https://img.youtube.com/vi/D7fKIbbwWLo/maxresdefault.jpg"
            ),

            // ─── COMEDY & CULTURE ────────────────────────────────────
            PodcastEpisode(
                id = "USqf4vuh5cY",
                title = "Kill Tony #569 – Theo Von",
                description = "Theo Von joins Tony Hinchcliffe and Brian Redban on stage as guest co-host for an incredible night of stand-up comedy and roast sessions.",
                host = "Kill Tony",
                category = "Comedy & Culture",
                thumbnailUrl = "https://img.youtube.com/vi/USqf4vuh5cY/maxresdefault.jpg"
            ),
            PodcastEpisode(
                id = "JEtRMUBVhvg",
                title = "Kamala Harris Interview",
                description = "Vice President Kamala Harris sits down with Alex Cooper for a historic, wide-ranging discussion on social issues, women's rights, and public service.",
                host = "Call Her Daddy",
                category = "Comedy & Culture",
                thumbnailUrl = "https://img.youtube.com/vi/JEtRMUBVhvg/maxresdefault.jpg"
            ),
            PodcastEpisode(
                id = "v9cvzW0m_ZU",
                title = "Joe Rogan – Episode #403",
                description = "Joe Rogan joins Theo Von to discuss comedy, life in Austin, hunting, physical fitness, and their legendary friendship.",
                host = "This Past Weekend",
                category = "Comedy & Culture",
                thumbnailUrl = "https://img.youtube.com/vi/v9cvzW0m_ZU/maxresdefault.jpg"
            ),
            PodcastEpisode(
                id = "P7p7-fbcHJY",
                title = "Matthew McConaughey Episode",
                description = "The iconic Matthew McConaughey joins Jason Bateman, Sean Hayes, and Will Arnett to discuss acting, storytelling, and philosophy.",
                host = "SmartLess",
                category = "Comedy & Culture",
                thumbnailUrl = "https://img.youtube.com/vi/P7p7-fbcHJY/maxresdefault.jpg"
            ),
            PodcastEpisode(
                id = "EYnDLRaelk8",
                title = "Trump Convicted Episode",
                description = "The MeidasTouch hosts analyze the historic legal verdict, its constitutional implications, and the unfolding political landscape.",
                host = "MeidasTouch Podcast",
                category = "Comedy & Culture",
                thumbnailUrl = "https://img.youtube.com/vi/EYnDLRaelk8/maxresdefault.jpg"
            ),
            PodcastEpisode(
                id = "_pRTsBQSAH4",
                title = "Vladimir Putin Interview",
                description = "Tucker Carlson travels to Moscow for a historic, extended interview with Russian President Vladimir Putin discussing global conflicts and history.",
                host = "The Tucker Carlson Show",
                category = "Comedy & Culture",
                thumbnailUrl = "https://img.youtube.com/vi/_pRTsBQSAH4/maxresdefault.jpg"
            ),
            PodcastEpisode(
                id = "8sPvUCcnA7I",
                title = "Kobe Bryant Crash Investigation",
                description = "Stephanie Soo provides a detailed, respectful look into the investigation, transcripts, and timeline of the tragic Kobe Bryant helicopter crash.",
                host = "Rotten Mango",
                category = "Comedy & Culture",
                thumbnailUrl = "https://img.youtube.com/vi/8sPvUCcnA7I/maxresdefault.jpg"
            ),
            PodcastEpisode(
                id = "9T9p2sPdOxA",
                title = "Andrew Santino Roasts Bobby Lee",
                description = "Bobby Lee and Andrew Santino engage in their classic chaotic banter, roasting each other's career moves, relationships, and quirks.",
                host = "Bad Friends",
                category = "Comedy & Culture",
                thumbnailUrl = "https://img.youtube.com/vi/9T9p2sPdOxA/maxresdefault.jpg"
            ),
            PodcastEpisode(
                id = "3w0V0JfbNOM",
                title = "Bill Burr Episode",
                description = "Stand-up legend Bill Burr joins Conan to roast modern culture, marriage, cooking, and the joys of losing your temper.",
                host = "Conan O'Brien Needs a Friend",
                category = "Comedy & Culture",
                thumbnailUrl = "https://img.youtube.com/vi/3w0V0JfbNOM/maxresdefault.jpg"
            )
        )
    }

    fun playEpisode(episode: PodcastEpisode) {
        _activeEpisode.value = episode
    }
}
