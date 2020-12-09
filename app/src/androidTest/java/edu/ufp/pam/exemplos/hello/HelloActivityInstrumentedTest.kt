package edu.ufp.pam.exemplos.hello

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import edu.ufp.pam.exemplos.R
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


/**
 * Instrumented test, which will execute on an Android device.
 * See [testing documentation](http://d.android.com/tools/testing).
 *
 * Turn off animations from Settings, i.e., open Developer options and turn off all options:
 *  - Window animation scale
 *  - Transition animation scale
 *  - Animator duration scale
 *
 * Procedure:
 *  1. Find UI component to test in an Activity (e.g., sign-in button in the app), by calling
 *     onView(), or onData() method for AdapterView controls.
 *  2. Simulate a user interaction on the UI component, by calling the ViewInteraction.perform()
 *     or DataInteraction.perform() method and passing in user action (e.g., click sign-in button).
 *     To sequence multiple actions, chain them using a comma-separated list in method argument.
 *  3. Repeat above steps as necessary, to simulate a user flow across multiple activities in app.
 *  4. Use ViewAssertions methods to check that UI reflects the expected state or behavior,
 *     after these user interactions are performed.
 *
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class HelloActivityInstrumentedTest {
    private lateinit var stringToBeChecked: String

    @get:Rule
    var activityRule: ActivityScenarioRule<MainHelloActivity>
            = ActivityScenarioRule(MainHelloActivity::class.java)

    @Before
    fun initValidValues() {
        stringToBeChecked = "Hello World on Android"
    }

    @Test
    fun checkTextViewHelloContent() {
        //Chek if textViewHello is displayed
        onView(withId(R.id.textView3))
            .perform(click()).check(matches(isDisplayed()))
        //Check that content of TextView is same as stringToBeChecked
        onView(withId(R.id.textView3))
            .check(matches(withText(stringToBeChecked)))
    }

    @Test
    fun checkEditTextHelloContent() {
        //val str = InstrumentationRegistry.getInstrumentation().context.getString(R.string.hello_world_on_android);
        //Write something else on the EditText
        onView(withId(R.id.editTextHello))
            .perform(clearText(), typeText("typed something"));
        //Check if content of EditView is the same as stringToBeChecked... WILL FAIL ON PURPOSE!!
        onView(withId(R.id.editTextHello))
            .check(matches(withText(stringToBeChecked)))
    }
}
