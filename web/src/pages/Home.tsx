import { Typography } from "@material-tailwind/react";

export default function Home() {
    return (
        <div className="home">
            <Typography
                as="big"
                variant="h1"
                className="text-5xl mr-4 cursor-pointer py-1.5 font-normal">
                    WELCOME BACK.
            </Typography>

            <br />

            <Typography
                as="big"
                variant="h1"
                className="text-3xl mr-4 cursor-pointer py-1.5 font-normal">
                    <span className="bg-purple-600 text-white p-1 rounded">Our app</span> brings an <span className="bg-yellow-600 text-white p-1 rounded">unique set</span> of features that combines <span className="underline underline-offset-4 decoration-purple-600">social media</span>, <span className="underline underline-offset-4 decoration-purple-600">video editing platform</span>, and a <span className="underline underline-offset-4 decoration-purple-600">daily habit tracker</span>.
            </Typography>

            <br />

            <button type="button" className="inline-block mt-2 px-6 py-2 border-2 border-gray-800 text-gray-800 text-xs font-bold leading-tight uppercase rounded-full hover:bg-black hover:bg-opacity-5 focus:outline-none focus:ring-0 transition duration-150 ease-in-out">
                <span>Development in Progress</span>
            </button>
            
            <table className="mt-5"><tr>

                <td valign="middle"><img width="500" alt="Screenshot-3" src="https://user-images.githubusercontent.com/35755386/213597146-7b062310-df93-488d-9dc6-e84036f26eb4.png" /></td>

                <td valign="middle"><img width="500" alt="Screenshot-4" src="https://user-images.githubusercontent.com/35755386/213597161-cd6da778-b4bc-4d40-ba5e-7a4c33982afd.png" /></td>

                <td valign="middle"><img width="500" alt="Screenshot-5" src="https://user-images.githubusercontent.com/35755386/213597165-01129c78-c6d7-4886-b257-91c5bb1b5162.png" /></td>

                <td valign="middle"><img width="500" alt="Screenshot-7" src="https://user-images.githubusercontent.com/35755386/213597182-8f16f327-549c-4aa2-a604-25315db092de.png" /></td>

            </tr></table>
        </div>
    );
}