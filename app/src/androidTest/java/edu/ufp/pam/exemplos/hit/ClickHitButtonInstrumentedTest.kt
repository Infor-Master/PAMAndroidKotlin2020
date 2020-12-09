package edu.ufp.pam.exemplos.hit

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import edu.ufp.pam.exemplos.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class ClickHitButtonInstrumentedTest {
    @get:Rule
    var activityRule: ActivityScenarioRule<MainHitActivity>
            = ActivityScenarioRule(MainHitActivity::class.java)

    @Test
    fun checkTextViewButtonHitContent() {
        //Chek if textView is displayed
        onView(withId(R.id.textViewHit))
            .perform(click()).check(matches(isDisplayed()))



        //Check that content of TextView is 0
        onView(withId(R.id.textViewHit))
            .check(matches(withText("0")))

        onView(withId(R.id.buttonHit))
            .perform(click()).check(matches(isDisplayed()))

        //Check that content of TextView has increased
        onView(withId(R.id.textViewHit))
            .check(matches(withText("1")))
    }

}